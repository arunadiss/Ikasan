/* 
 * $Id$
 * $URL$
 *
 * =============================================================================
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
 * =============================================================================
 */
package org.ikasan.component.converter.jms;

import org.ikasan.spec.component.transformation.TransformationException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.Test;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import javax.xml.transform.TransformerException;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link org.ikasan.component.converter.jms.TextMessageToStringConverter}
 * 
 * @author Ikasan Development Team
 * 
 */
@SuppressWarnings("unqualified-field-access")
public class TextMessageToStringConverterTest
{

    /** Mockery for objects */
    private final Mockery mockery = new Mockery()
    {
        {
            setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        }
    };

    private final TextMessage textMessage = this.mockery.mock(TextMessage.class, "mockTextMessage");

    /** The test object */
    private TextMessageToStringConverter uut = new TextMessageToStringConverter();



    @Test
    public void convert_with_message() throws TransformerException, JMSException {
        final String message = "TextMessage";
        // Setup expectations
        this.mockery.checking(new Expectations()
        {
            {
                exactly(1).of(textMessage).getText();
                will(returnValue(message));
            }
        });

        // Run the test
        String result = uut.convert(textMessage);

        // Make assertions
        this.mockery.assertIsSatisfied();
        assertEquals(message,result);
    }

    @Test(expected = TransformationException.class)
    public void convert_with_message_throwing_JMSException() throws TransformerException, JMSException {
        final String message = "TextMessage";
        // Setup expectations
        this.mockery.checking(new Expectations()
        {
            {
                exactly(1).of(textMessage).getText();
                will(throwException(new JMSException("Failed to get textMessage")));
            }
        });

        // Run the test
        String result = uut.convert(textMessage);

        // Make assertions
        this.mockery.assertIsSatisfied();
    }
}
