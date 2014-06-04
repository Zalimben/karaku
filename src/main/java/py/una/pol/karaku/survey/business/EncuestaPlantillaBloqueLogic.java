/*
 * @EncuestaPlantillaBloqueLogic 1.0 29/05/13. Sistema Integral de Gestion
 * Hospitalaria
 */
package py.una.pol.karaku.survey.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.una.pol.karaku.business.KarakuBaseLogic;
import py.una.pol.karaku.repo.IKarakuBaseDao;
import py.una.pol.karaku.survey.domain.EncuestaPlantilla;
import py.una.pol.karaku.survey.domain.EncuestaPlantillaBloque;
import py.una.pol.karaku.survey.repo.IEncuestaPlantillaBloqueDAO;

/**
 * 
 * 
 * @author Nathalia Ochoa
 * @since 1.0
 * @version 1.0 29/05/2013
 * 
 */
@Service
@Transactional
public class EncuestaPlantillaBloqueLogic extends
		KarakuBaseLogic<EncuestaPlantillaBloque, Long> implements
		IEncuestaPlantillaBloqueLogic {

	@Autowired
	private IEncuestaPlantillaBloqueDAO dao;

	@Override
	public IKarakuBaseDao<EncuestaPlantillaBloque, Long> getDao() {

		return dao;
	}

	@Override
	public List<EncuestaPlantillaBloque> getBlocksByTemplate(
			EncuestaPlantilla template) {

		return dao.getBlocksByTemplate(template);
	}
}
