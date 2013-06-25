/*
 * @WSEndpointDAO.java 1.0 Jun 11, 2013 Sistema Integral de Gestion Hospitalaria
 */
package py.una.med.base.services.client;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import py.una.med.base.repo.SIGHBaseDao;

/**
 * 
 * @author Arturo Volpe
 * @since 1.0
 * @version 1.0 Jun 11, 2013
 * 
 */
@Repository
@Transactional
public class WSEndpointDAO extends SIGHBaseDao<WSEndpoint, Long> {

}
