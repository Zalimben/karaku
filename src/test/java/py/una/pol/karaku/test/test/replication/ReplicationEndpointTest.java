/*-
 * Copyright (c)
 *
 * 		2012-2014, Facultad Polit�cnica, Universidad Nacional de Asunci�n.
 * 		2012-2014, Facultad de Ciencias M�dicas, Universidad Nacional de Asunci�n.
 * 		2012-2013, Centro Nacional de Computaci�n, Universidad Nacional de Asunci�n.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package py.una.pol.karaku.test.test.replication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.test.server.ResponseMatchers;
import py.una.pol.karaku.replication.server.Bundle;
import py.una.pol.karaku.replication.server.DummyFirstChangeProvider;
import py.una.pol.karaku.replication.server.DummyReplicationProvider;
import py.una.pol.karaku.replication.server.FirstChangeProviderHandler;
import py.una.pol.karaku.replication.server.ReplicationProvider;
import py.una.pol.karaku.services.ConverterProvider;
import py.una.pol.karaku.test.base.BaseTestWebService;
import py.una.pol.karaku.test.configuration.TransactionTestConfiguration;
import py.una.pol.karaku.test.configuration.WebServiceTestConfiguration;
import py.una.pol.karaku.test.test.replication.layers.ReplicatedEntity;
import py.una.pol.karaku.test.test.replication.layers.ReplicationEntityEndpoint;
import py.una.pol.karaku.test.test.replication.layers.ReplicationEntityRequest;
import py.una.pol.karaku.test.test.replication.layers.ReplicationEntityResponse;
import py.una.pol.karaku.test.util.TestUtils;
import py.una.pol.karaku.test.util.transaction.DatabasePopulatorExecutionListener;
import py.una.pol.karaku.test.util.transaction.SQLFiles;

/**
 * 
 * @author Arturo Volpe
 * @since 2.2.8
 * @version 1.0 Nov 8, 2013
 * 
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@SQLFiles("ReplicationProviderTest")
@TestExecutionListeners({ TransactionalTestExecutionListener.class,
		DatabasePopulatorExecutionListener.class })
public class ReplicationEndpointTest extends BaseTestWebService {

	@Configuration
	public static class ContextConfiguration extends
			WebServiceTestConfiguration {

		@Override
		public Class<?>[] getClassesToBound() {

			return TestUtils.getAsClassArray(ReplicationEntityRequest.class,
					ReplicationEntityResponse.class);
		}

		@Bean
		ReplicationEntityEndpoint endpoint() {

			return new ReplicationEntityEndpoint();
		}

		@Bean
		ReplicationProvider provider() {

			return new DummyReplicationProvider();
		}

		@Bean
		ConverterProvider converterProvider() {

			return new ConverterProvider();
		}

		@Bean
		FirstChangeProviderHandler changeProviderHandler() {

			return new FirstChangeProviderHandler();
		}

		@Bean
		DummyFirstChangeProvider dummyFirstChangeProvider() {

			return new DummyFirstChangeProvider();
		}
	}

	@Configuration
	public static class TransactionConfiguration extends
			TransactionTestConfiguration {

		@Override
		public Class<?>[] getEntityClasses() {

			return TestUtils.getReferencedClasses(ReplicatedEntity.class);
		}
	}

	@Test
	@Transactional
	public void testCallMocked() {

		ReplicationEntityRequest mr = new ReplicationEntityRequest();
		mr.setLastId(Bundle.ZERO_ID);
		sendRequest(mr).andExpect(ResponseMatchers.noFault());
		ReplicationEntityResponse response = getResponse();
		assertNotNull("Null response", response);
		assertNotNull("Response with null entities", response.getEntities());
		assertEquals(5, response.getEntities().size());
	}

}
