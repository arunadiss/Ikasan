package org.ikasan.persistence.service;

import jakarta.annotation.Resource;
import org.h2.tools.Server;
import org.ikasan.persistence.InDoubtTransactionAutoConfiguration;
import org.ikasan.persistence.InDoubtTransactionTestAutoConfiguration;
import org.ikasan.spec.persistence.model.InDoubtTransaction;
import org.ikasan.spec.persistence.service.InDoubtTransactionService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InDoubtTransactionAutoConfiguration.class, InDoubtTransactionTestAutoConfiguration.class})
public class InDoubtTransactionServiceImplTest {

    @Resource
    DataSource ikasanDataSource;

    Server server;

    @Before
    public void setup() throws IOException, SQLException {
        server = Server.createTcpServer("-tcpPort", Integer.toString(18082), "-tcpAllowOthers", "-ifNotExists");
        server.start();

        this.createInDoubtTransaction("lobtx");
    }

    @After
    public void teardown() throws IOException {
        server.stop();
        this.cleanDirectory("./target/persistence");
    }

    @Autowired
    InDoubtTransactionService inDoubtTransactionService;
    @Test
    public void test_get_in_doubt_transactions() {
        Assert.assertNotNull(inDoubtTransactionService.getInDoubtTransactions());
        Assert.assertEquals("There should be 1 in doubt transaction!"
            , 1, inDoubtTransactionService.getInDoubtTransactions().size());

        Assert.assertEquals("The transaction name should equal LOBTX!"
            , "LOBTX", inDoubtTransactionService.getInDoubtTransactions().get(0).getTransactionName());

        Assert.assertEquals("The transaction state should equal IN_DOUBT!"
            , "IN_DOUBT", inDoubtTransactionService.getInDoubtTransactions().get(0).getTransactionState());
    }

    @Test
    public void test_get_in_doubt_transaction() {
        Assert.assertNotNull("Should return the in doubt transaction!"
            , inDoubtTransactionService.getInDoubtTransaction("lobtx"));

        Assert.assertNull("Should NOT return the in doubt transaction!"
            , inDoubtTransactionService.getInDoubtTransaction("bad transaction name!"));
    }

    @Test
    public void test_commit_in_doubt_transaction_success() throws SQLException {
        InDoubtTransaction inDoubtTransaction = inDoubtTransactionService.getInDoubtTransaction("lobtx");
        inDoubtTransactionService.commitInDoubtTransaction(inDoubtTransaction.getTransactionName());

        Assert.assertEquals("There should be 0 in doubt transactions!"
            , 0, inDoubtTransactionService.getInDoubtTransactions().size());

        Assert.assertEquals("We should have successfully committed the transaction with 100 records!"
            , 100, this.getNumLobRecords());
    }

    @Test
    public void test_commit_all_in_doubt_transactions_success() throws SQLException {
        Assert.assertEquals("There should be 1 in doubt transactions!"
            , 1, inDoubtTransactionService.getInDoubtTransactions().size());

        inDoubtTransactionService.commitAllInDoubtTransactions();

        Assert.assertEquals("There should be 0 in doubt transactions!"
            , 0, inDoubtTransactionService.getInDoubtTransactions().size());

        Assert.assertEquals("We should have successfully committed the transaction with 100 records!"
            , 100, this.getNumLobRecords());
    }

    @Test(expected = RuntimeException.class)
    public void test_commit_in_doubt_transaction_exception_bad_transaction_name() {
        inDoubtTransactionService.commitInDoubtTransaction("BAD TRANSACTION NAME!");
    }

    @Test
    public void test_rollback_in_doubt_transaction_success() throws SQLException {
        InDoubtTransaction inDoubtTransaction = inDoubtTransactionService.getInDoubtTransaction("lobtx");
        inDoubtTransactionService.rollbackInDoubtTransaction(inDoubtTransaction.getTransactionName());

        Assert.assertEquals("There should be 0 in doubt transactions!"
            , 0, inDoubtTransactionService.getInDoubtTransactions().size());

        Assert.assertEquals("We should have successfully rolled back the transaction!"
            , 0, this.getNumLobRecords());
    }

    @Test
    public void test_rollback_all_in_doubt_transactions_success() throws SQLException {
        Assert.assertEquals("There should be 1 in doubt transactions!"
            , 1, inDoubtTransactionService.getInDoubtTransactions().size());

        inDoubtTransactionService.rollbackAllInDoubtTransactions();

        Assert.assertEquals("There should be 0 in doubt transactions!"
            , 0, inDoubtTransactionService.getInDoubtTransactions().size());

        Assert.assertEquals("We should have successfully rolled back the transaction!"
            , 0, this.getNumLobRecords());
    }

    @Test(expected = RuntimeException.class)
    public void test_rollback_in_doubt_transaction_exception_bad_transaction_name() {
        inDoubtTransactionService.rollbackInDoubtTransaction("BAD TRANSACTION NAME!");
    }

    private int getNumLobRecords() throws SQLException {
        try(Connection connection = this.ikasanDataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT COUNT(*) AS COUNT FROM dataTable");

            resultSet.next();
            return resultSet.getInt("COUNT");
        }
    }

    /**
     * Helper method to clean a directory.
     *
     * @param directory
     * @throws IOException
     */
    private void cleanDirectory(String directory) throws IOException {
        Path dir = Paths.get(directory);

        List<Path> files = Files
            .walk(dir)
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        for(Path file: files) {
            Files.delete(file);
        }
    }

    private void createInDoubtTransaction(String transactionName) {
        org.h2.Driver.load();

        try (Connection conn = DriverManager.getConnection
            ("jdbc:h2:tcp://localhost:18082/./target/persistence/esb;IFEXISTS=FALSE;NON_KEYWORDS=VALUE"
                , "sa", "sa")) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS dataTable("
                    + "dataStamp BIGINT PRIMARY KEY, "
                    + "data BLOB)");
            }

            conn.setAutoCommit(false);
            Random rnd = new Random(0);
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO dataTable VALUES(?, ?)")) {
                for (int i = 0; i < 100; ++i) {
                    int numBytes = 1024 * 1024;
                    byte[] data = new byte[numBytes];
                    rnd.nextBytes(data);
                    pstmt.setLong(1, i);
                    pstmt.setBytes(2, data);
                    pstmt.executeUpdate();
                }
            }
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("PREPARE COMMIT " + transactionName);
                st.execute("SHUTDOWN IMMEDIATELY");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
