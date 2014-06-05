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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import py.una.pol.karaku.model.KarakuRevisionEntity;
import py.una.pol.karaku.replication.EntityNotFoundException;
import py.una.pol.karaku.replication.client.CacheAll;
import py.una.pol.karaku.replication.client.IReplicationLogic;
import py.una.pol.karaku.replication.client.ReplicationHandler;
import py.una.pol.karaku.replication.client.ReplicationInfo;
import py.una.pol.karaku.replication.client.ReplicationInfoDao;
import py.una.pol.karaku.replication.client.ReplicationLogic;
import py.una.pol.karaku.replication.client.ReplicationRequestFactory;
import py.una.pol.karaku.replication.client.ReplicationResponseHandler;
import py.una.pol.karaku.services.AbstractConverter;
import py.una.pol.karaku.services.ConverterProvider;
import py.una.pol.karaku.services.client.WSSecurityInterceptor;
import py.una.pol.karaku.test.base.BaseTestWithDatabase;
import py.una.pol.karaku.test.configuration.TransactionTestConfiguration;
import py.una.pol.karaku.test.test.replication.layers.ReplicatedEntity;
import py.una.pol.karaku.test.test.replication.layers.ReplicatedEntityDao;
import py.una.pol.karaku.test.test.replication.layers.ReplicatedEntityResponse;
import py.una.pol.karaku.test.util.TestDateProvider;
import py.una.pol.karaku.test.util.TestPropertiesUtil;
import py.una.pol.karaku.test.util.TestUriCache;
import py.una.pol.karaku.test.util.TestUtils;
import py.una.pol.karaku.test.util.transaction.SQLFiles;

/**
 * 
 * @author Arturo Volpe
 * @since 2.2.8
 * @version 1.0 Nov 22, 2013
 * 
 */
@SQLFiles
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ReplicationHandlerTest extends BaseTestWithDatabase {

	@Configuration
	@EnableTransactionManagement()
	static class ContextConfiguration extends TransactionTestConfiguration {

		@Override
		public Class<?>[] getEntityClasses() {

			return TestUtils.getReferencedClasses(ReplicatedEntity.class,
					ReplicationInfo.class, KarakuRevisionEntity.class);
		}

		@Bean
		ReplicationHandler replicationHandler() {

			return new ReplicationHandler();
		}

		@Bean
		IReplicationLogic iReplicationLogic() {

			return new ReplicationLogic();
		}

		@Bean
		ReplicationInfoDao dao() {

			return new ReplicationInfoDao();
		}

		@Bean
		WSSecurityInterceptor interceptor() {

			return new WSSecurityInterceptor();
		}

		@Bean
		WebServiceTemplate serviceTemplate() {

			return new WSTemplate();
		}

		@Bean
		ConverterProvider converterProvider() {

			return new ConverterProvider();
		}

		@Bean
		ReplicatedEntityDao entityDao() {

			return new ReplicatedEntityDao();
		}

		@Bean
		ReplicationRequestFactory requestFactory() {

			return new ReplicationRequestFactory();
		}

		@Bean
		ReplicationResponseHandler responseHandler() {

			return new ReplicationResponseHandler();
		}

		@Override
		protected boolean getWithEnvers() {

			return true;
		}

		@Bean
		TestUriCache testUriCache() {

			return new TestUriCache();
		}

		@Bean
		ReplicatedEntityConverter replicatedEntityConverter() {

			return new ReplicatedEntityConverter();
		}

	}

	@Autowired
	private TestDateProvider dateProvider;

	@Autowired
	private ReplicationHandler replicationHandler;

	@Autowired
	private IReplicationLogic logic;

	@Autowired
	private ReplicatedEntityDao dao;

	@Autowired
	private WSTemplate template;

	@Autowired
	private TestPropertiesUtil propertiesUtil;

	@Before
	public void activePermissions() {

		propertiesUtil.put(ReplicationHandler.REPLICATION_ENABLED, "true");
		propertiesUtil.put("karaku.ws.client.enabled", "true");
	}

	@Test
	public void testDoSync() throws Exception {

		ReplicatedEntity re1 = new ReplicatedEntity();
		re1.setDescription("DESC1");
		re1.setUri("re1");

		ReplicatedEntity re2 = new ReplicatedEntity();
		re2.setUri("re2");
		re2.setDescription("222");

		template.addReplicatedEntity(re1);

		replicationHandler.doSync();
		// the first is skiped
		replicationHandler.doSync();
		assertTrue(logic.getReplicationsToDo().isEmpty());
		assertThat(dao.getCount(), is(1L));
		assertThat(dao.getAll(null), hasItem(re1));

		// minutos 2
		// limpiamos todo y agregamos dos, una de las cuales ya es vieja
		dateProvider.forward(2);
		template.clear();

		re1.setDescription("updated");

		template.addReplicatedEntity(re1);
		template.addReplicatedEntity(re2);

		assertFalse("dont have changes to sync", logic.getReplicationsToDo()
				.isEmpty());
		replicationHandler.doSync();
		assertTrue(logic.getReplicationsToDo().isEmpty());
		assertThat(dao.getCount(), is(2L));
		assertThat(dao.getAll(null), hasItems(re1, re2));

	}

	//
	@CacheAll
	public static class ReplicatedEntityConverter extends
			AbstractConverter<ReplicatedEntity, ReplicatedEntity> {

		@Override
		public ReplicatedEntity toEntity(ReplicatedEntity dto)
				throws EntityNotFoundException {

			return dto;
		}

		@Override
		public ReplicatedEntity toDTO(ReplicatedEntity entity, int depth) {

			return entity;
		}
	}

	private static class WSTemplate extends WebServiceTemplate {

		List<ReplicatedEntity> entities = new ArrayList<ReplicatedEntity>();

		@Override
		public Object marshalSendAndReceive(String uri, Object requestPayload,
				WebServiceMessageCallback requestCallback) {

			return getEntityResponse();
		}

		public void clear() {

			entities.clear();
		}

		public void addReplicatedEntity(ReplicatedEntity re) {

			entities.add(re);
		}

		private ReplicatedEntityResponse getEntityResponse() {

			ReplicatedEntityResponse toRet = new ReplicatedEntityResponse();
			toRet.setEntities(entities);
			return toRet;
		}
	}

}
