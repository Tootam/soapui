/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Topic;
import javax.jms.TopicSession;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HermesJmsRequestPublishReceiveTransport extends HermesJmsRequestTransport
{

	public Response execute( SubmitContext submitContext, Request request, long timeStarted ) throws Exception
	{
		TopicSession topicSession = null;
		QueueSession queueSession = null;
		JMSConnectionHolder jmsConnectionHolder = null;
		try
		{
			init( submitContext, request );
			jmsConnectionHolder = new JMSConnectionHolder( jmsEndpoint, hermes, true, true, clientID, username, password );

			// session
			topicSession = jmsConnectionHolder.getTopicSession();
			queueSession = jmsConnectionHolder.getQueueSession();

			// destination
			Topic topicPublish = jmsConnectionHolder.getTopic( jmsConnectionHolder.getJmsEndpoint().getSend() );
			Queue queueReceive = jmsConnectionHolder.getQueue( jmsConnectionHolder.getJmsEndpoint().getReceive() );

			Message messagePublish = messagePublish( submitContext, request, topicSession,
					jmsConnectionHolder.getHermes(), topicPublish );

			MessageConsumer messageConsumer = queueSession.createConsumer( queueReceive, messageSelector );

			return makeResponse( submitContext, request, timeStarted, messagePublish, messageConsumer );
		}
		catch( JMSException jmse )
		{
			return errorResponse( submitContext, request, timeStarted, jmse );
		}
		catch( Throwable t )
		{
			SoapUI.logError( t );
		}
		finally
		{
			closeSessionAndConnection( jmsConnectionHolder != null ? jmsConnectionHolder.getQueueConnection() : null,queueSession );
			closeSessionAndConnection( jmsConnectionHolder != null ? jmsConnectionHolder.getTopicConnection() : null,topicSession );
		}
		return null;

	}
}
