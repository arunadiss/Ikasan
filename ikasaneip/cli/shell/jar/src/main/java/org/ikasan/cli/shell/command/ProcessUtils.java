/*
 * $Id$
 * $URL$
 *
 * ====================================================================
 * Ikasan Enterprise Integration Platform
 *
 * Distributed under the Modified BSD License.
 * Copyright notice: The copyright for this software and a full listing
 * of individual contributors are as shown in the packaged copyright.txt
 * file.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  - Neither the name of the ORGANIZATION nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.ikasan.cli.shell.command;

import org.ikasan.cli.shell.reporting.ProcessInfo;
import org.ikasan.cli.shell.reporting.ProcessInfos;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Process utility class.
 *
 * @author Ikasan Development Team
 */
public class ProcessUtils
{
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    public static String CLASSPATH_SEPARATOR = System.getProperty("path.separator");
    public static String MINUS_D_MODULE_NAME = "-Dmodule.name=";
    public static String CLASSPATH = "-classpath";
    public static String CP = "-cp";
    public static String JAR = "-jar";
    public static String WILDCARD = "*";

    /**
     * Retrieves information about a process.
     *
     * @param process The process object from which to retrieve information. Must not be null.
     * @param name The name of the process. Can be null.
     * @return A string containing the information about the process. Returns null if the process is null or the process info is not available.
     */
    public static String getProcessInfo(Process process, String name)
    {
        return getProcessInfo(null, process, name);
    }

    /**
     * Creates an instance of ProcessInfo.
     *
     * @return A new instance of ProcessInfo.
     */
    public static ProcessInfo createProcessInfo()
    {
        return new ProcessInfo();
    }

    /**
     * Creates an instance of ProcessInfos.
     *
     * @return A new instance of ProcessInfos.
     */
    public static ProcessInfos createProcessInfos()
    {
        return new ProcessInfos();
    }

    /**
     * Retrieves information about a process.
     *
     * @param type The type of the process. Can be null.
     * @param process The process object from which to retrieve information. Must not be null.
     * @param name The name of the process. Can be null.
     * @return A string containing the information about the process. Returns null if the process is null
     * or the process info is not available.
     */
    public static String getProcessInfo(String type, Process process, String name)
    {
        if(process == null)
        {
            return null;
        }

        ProcessHandle.Info processHandleInfo = process.info();
        if(processHandleInfo != null)
        {
            StringBuilder sb = new StringBuilder();
            if(type != null)
            {
                sb.append(type + " " + name + " process started");
            }

            if(processHandleInfo.command().isPresent())
            {
                sb.append(" [" + processHandleInfo.command().get() + "]");
            }

            if(processHandleInfo.user().isPresent())
            {
                sb.append(" as user[" + processHandleInfo.user().get() + "]");
            }

            Optional<String> commandLine = processHandleInfo.commandLine();
            if(commandLine.isPresent())
            {
                sb.append(" Command Line[" + commandLine.get() + "]");
            }

            return sb.toString();
        }

        return null;
    }

    /**
     * This method retrieves a list of commands based on the command string and module name.
     *
     * @param commandStr The command string to parse. Can be null.
     * @param moduleName The name of the module. Can be null.
     *
     * @return A list of commands. Returns an empty list if the command string is null.
     */
    public static List<String> getCommands(String commandStr, String moduleName)
    {
        if(commandStr == null)
        {
            return new ArrayList<String>();
        }

        List<String> commands = getCommands(commandStr);
        if(!commandStr.contains(MINUS_D_MODULE_NAME))
        {
            commands.add(1, "MINUS_D_MODULE_NAME"+moduleName);
        }

        return commands;
    }

    /**
     * This method retrieves a list of commands based on the command string.
     *
     * @param commandStr The command string to parse. Can be null.
     *
     * @return A list of commands. Returns an empty list if the command string is null.
     */
    public static List<String> getCommands(String commandStr)
    {
        if(commandStr == null)
        {
            return new ArrayList<String>();
        }

        String[] commands = commandStr.split("\\s+");
        for(int i = 0; i < commands.length; i++)
        {
            if(CLASSPATH.equals(commands[i].toLowerCase()) || CP.equals(commands[i].toLowerCase()))
            {
                commands[i+1] = expandWildcardNotation( commands[i+1].split(CLASSPATH_SEPARATOR) );
            }
            else if(JAR.equals(commands[i].toLowerCase()))
            {
                commands[i+1] = expandWildcardNotation( commands[i+1] );
            }
        }
        return new ArrayList<>(Arrays.asList(commands));
    }

    /**
     * Expands wildcard notation in a file path.
     *
     * @param wildcardNotation The file path with wildcard notation. Must not be null.
     * @return The expanded file path without wildcard notation. Returns the original file path if there are no wildcards.
     * @throws RuntimeException if there is an error expanding the wildcard notation.
     */
    protected static String expandWildcardNotation(String wildcardNotation)
    {
        if(wildcardNotation.contains(WILDCARD))
        {
            // Do not use NIO for sourcing file systems as a full path as any wildcards cause issues
            File file = new File(wildcardNotation);
            String parent = file.getParent();
            String name = file.getName();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(parent), name))
            {
                for (Path path: stream)
                {
                    // return the first as there should only be one match
                    return path.toString();
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException("Unable to expand wildcard notation for " + wildcardNotation, e);
            }
        }

        return wildcardNotation;
    }


    /**
     * Expands wildcard notation in an array of file paths.
     *
     * @param wildcardNotationParts An array of file paths with wildcard notation. Must not be null.
     * @return The expanded file paths without wildcard notation. Returns the original file paths if there are no wildcards.
     * @throws RuntimeException if there is an error expanding the wildcard notation.
     */
    protected static String expandWildcardNotation(String[] wildcardNotationParts)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for(String wildcardNotationPart:wildcardNotationParts)
        {
            stringBuilder.append( expandWildcardNotation(wildcardNotationPart) );
            stringBuilder.append(CLASSPATH_SEPARATOR);
        }

        return stringBuilder.toString();
    }

}