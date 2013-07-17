/*
 * @SIGHDynamicSurveyBaseController.java 1.0 03/06/13. Sistema Integral de
 * Gestion Hospitalaria
 */
package py.una.med.base.survey.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.WebApplicationContext;
import py.una.med.base.business.ISIGHBaseLogic;
import py.una.med.base.survey.business.IEncuestaDetalleLogic;
import py.una.med.base.survey.business.IEncuestaPlantillaBloqueLogic;
import py.una.med.base.survey.business.IEncuestaPlantillaPreguntaLogic;
import py.una.med.base.survey.components.DynamicSurveyBlock;
import py.una.med.base.survey.components.DynamicSurveyDataTable;
import py.una.med.base.survey.components.DynamicSurveyField;
import py.una.med.base.survey.components.DynamicSurveyField.SurveyField;
import py.una.med.base.survey.components.DynamicSurveyFieldOption;
import py.una.med.base.survey.components.DynamicSurveyFields;
import py.una.med.base.survey.components.DynamicSurveyRow;
import py.una.med.base.survey.domain.Encuesta;
import py.una.med.base.survey.domain.EncuestaDetalle;
import py.una.med.base.survey.domain.EncuestaDetalleOpcionRespuesta;
import py.una.med.base.survey.domain.EncuestaPlantilla;
import py.una.med.base.survey.domain.EncuestaPlantillaBloque;
import py.una.med.base.survey.domain.EncuestaPlantillaPregunta;
import py.una.med.base.survey.domain.OpcionRespuesta;
import py.una.med.base.survey.domain.TipoBloque;
import py.una.med.base.util.I18nHelper;

/**
 * 
 * 
 * @author Nathalia Ochoa
 * @since 1.0
 * @version 1.0 03/06/2013
 * 
 */
@Controller
@Scope(value = WebApplicationContext.SCOPE_SESSION)
public abstract class SIGHDynamicSurveyBaseController implements
		ISIGHDynamicSurveyBaseController {

	public static enum Mode {
		NEW, VIEW, DELETE
	}

	private Mode mode = Mode.VIEW;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Encuesta bean;
	private Encuesta lastSurvey;

	// Lista de bloques dentro de la encuesta
	private List<DynamicSurveyBlock> blocks;

	@Autowired
	private IEncuestaPlantillaPreguntaLogic templateQuestionLogic;
	@Autowired
	private IEncuestaPlantillaBloqueLogic templateBlockLogic;
	@Autowired
	private IEncuestaDetalleLogic responseLogic;

	@Override
	public abstract ISIGHBaseLogic<Encuesta, Long> getBaseLogic();

	/**
	 * Crea un bloque de acuerdo al tipo y obtiene las respuestas
	 * correspondientes a la encuesta
	 * 
	 * @param questions
	 * @param tipoBloque
	 * @param bloque
	 * @return
	 */
	public DynamicSurveyBlock createBlock(
			List<EncuestaPlantillaPregunta> questions, TipoBloque tipoBloque,
			EncuestaPlantillaBloque bloque) {

		/**
		 * Poner el tema de las respuestas para cada bloque
		 */
		if (tipoBloque.getDescripcion().equals("GRILLA")) {
			return buildResponseGrill(bloque, new DynamicSurveyDataTable(
					questions, blocks.size()));
		} else {
			return buildResponseFields(bloque, new DynamicSurveyFields(
					questions, blocks.size()));

		}

	}

	/**
	 * Agrega un bloque en particular en la estructura de la encuesta.
	 * 
	 * @param block
	 * @return
	 */
	public List<DynamicSurveyBlock> addBlock(DynamicSurveyBlock block) {

		blocks.add(block);
		return blocks;
	}

	/**
	 * Contruye la estructura de la encuesta, es decir los bloques que conforman
	 * la encuesta teniendo en cuenta el tipo de cada bloque.
	 */
	public void initBlocks() {

		this.blocks = new ArrayList<DynamicSurveyBlock>();
		buildBlocks();
	}

	@Override
	public List<DynamicSurveyBlock> getBlocks() {

		if (blocks == null) {
			initBlocks();
		}
		return blocks;
	}

	@Override
	public void setBlocks(List<DynamicSurveyBlock> blocks) {

		this.blocks = blocks;
	}

	/**
	 * Construye cada uno de los bloques que conforman la encuesta.
	 */
	public void buildBlocks() {

		for (EncuestaPlantillaBloque bloque : getBlocksByTemplate()) {

			addBlock(createBlock(getQuestionsByBlock(bloque),
					bloque.getTipoBloque(), bloque));
		}
		// Obtenemos los datos precargados del servicio de importar persona
		if (blocks.get(0) instanceof DynamicSurveyFields) {
			getDetailsPerson((DynamicSurveyFields) blocks.get(0));

		}

	}

	/**
	 * Obtiene los bloques que se encuentran asociados a una plantilla en
	 * particular, los cuales definen la estructura de la encuesta.
	 * 
	 * @return lista de bloques contenidos en la plantilla
	 */
	public List<EncuestaPlantillaBloque> getBlocksByTemplate() {

		return templateBlockLogic.getBlocksByTemplate(getTemplate());
	}

	/**
	 * Obtiene las preguntas que se encuentran en un bloque especifico el cual
	 * es recibido como parametro
	 * 
	 * @param block
	 *            bloque del cual se desea obtener la lista de preguntas
	 * @return lista de preguntas del bloque
	 */
	public List<EncuestaPlantillaPregunta> getQuestionsByBlock(
			EncuestaPlantillaBloque block) {

		return templateQuestionLogic.getQuestionsByBlock(block);
	}

	/**
	 * Crea una nueva encuesta, si la persona ya posee una encuesta se realiza
	 * una copia de la misma, de lo contrario se crea un nueva encuesta en
	 * blanco.
	 * 
	 * @param survey
	 *            Ultima encuesta de la persona
	 * @param template
	 *            Plantilla de la encuesta a dibujar
	 * @return
	 */
	public Encuesta newSurvey(Encuesta survey, EncuestaPlantilla template) {

		Encuesta newSurvey = getBaseLogic().getNewInstance();
		newSurvey.setPlantilla(template);
		newSurvey.setFechaRealizacion(new Date());
		if (survey == null) {
			log.debug("Se crea una nueva encuesta para la solicitud");
			setLastSurvey(getBaseLogic().getById(1L));
		} else {
			log.debug("Se utiliza una copia de la ultima encuesta");
			setLastSurvey(survey);
		}
		return newSurvey;
	}

	/**
	 * Carga las respuestas de un bloque en particular del tipo
	 * {@link DynamicSurveyDataTable}.
	 * 
	 * @param encuestaBloque
	 *            bloque del cual se desean obtener las respuestas
	 * @param block
	 *            bloque que almacenara las respuestas y las representara en el
	 *            dataTable
	 */
	public DynamicSurveyDataTable buildResponseGrill(
			EncuestaPlantillaBloque encuestaBloque, DynamicSurveyDataTable block) {

		List<DynamicSurveyRow> registros = new ArrayList<DynamicSurveyRow>();

		int columnsNumber = block.getColumnsNumber();
		int nroFila = 1;
		DynamicSurveyRow row = new DynamicSurveyRow(block.getId(), nroFila,
				columnsNumber);

		int ordenPregunta = 0;
		for (Object respuesta : getResponseBlock(encuestaBloque)) {

			Object[] detalle = (Object[]) respuesta;
			ordenPregunta = (Integer) detalle[0];
			if (nroFila != (Integer) detalle[1]) {
				registros.add(row);
				nroFila = (Integer) detalle[1];
				row = new DynamicSurveyRow(block.getId(), nroFila,
						columnsNumber);

			}

			SurveyField cell = DynamicSurveyField.fieldFactory(block
					.getQuestion(ordenPregunta));

			cell.setIndex(ordenPregunta);
			if (detalle[2] != null) {
				cell.setValue(detalle[2].toString());
			} else {
				cell.setValue("");
			}
			row.addCell(cell);

		}
		registros.add(row);

		return block.buildRows(registros);
	}

	/**
	 * Carga las respuestas asociadas a un bloque en particular del tipo
	 * {@link DynamicSurveyFields}.
	 * 
	 * @param encuestaBloque
	 * @param block
	 * @return
	 */
	public DynamicSurveyFields buildResponseFields(
			EncuestaPlantillaBloque encuestaBloque, DynamicSurveyFields block) {

		int ordenPregunta = 0;
		for (Object respuesta : getResponseBlock(encuestaBloque)) {
			Object[] detalle = (Object[]) respuesta;
			ordenPregunta = (Integer) detalle[0];
			DynamicSurveyFieldOption field = new DynamicSurveyFieldOption(
					block.getTypeField(ordenPregunta - 1));
			SurveyField cell = DynamicSurveyField.fieldFactory(block
					.getQuestion(ordenPregunta));

			cell.setIndex(ordenPregunta);
			if (detalle[2] != null) {
				cell.setValue(detalle[2].toString());
			}
			// Obtenemos las respuestas que estan asociadas a un/os detalle/s
			List<OpcionRespuesta> listResponse = responseLogic
					.getRespuestasSelected((Long) detalle[3]);

			if (field.getType().equals("CHECK")) {
				field.setManyOptions(listResponse);

			} else {
				if (field.getType().equals("RADIO")) {
					field.setOneOption(listResponse.get(0));
				}
			}

			field.setField(cell);
			block.addField(field);
		}

		return block.buildFields();
	}

	/**
	 * Obtiene las respuestas relacionadas a un bloque en particular, de una
	 * determinada encuesta.
	 * 
	 * @param encuestaBloque
	 *            Bloque del cual se desea obtener las respuestas.
	 * @return Lista de respuestas relacionadas al bloque recibido como
	 *         parametro.
	 */
	public List<?> getResponseBlock(EncuestaPlantillaBloque encuestaBloque) {

		return responseLogic.getRespuestas(getLastSurvey(), encuestaBloque);
	}

	/**
	 * Carga los datos de la persona en la encuesta.
	 * 
	 * @param block
	 *            bloque donde se deben cargar los datos de la persona
	 */
	public abstract void getDetailsPerson(DynamicSurveyFields block);

	@Override
	public void preCreate(EncuestaPlantilla template, Encuesta survey,
			boolean clone) {

		log.debug("preCreate llamado");
		mode = Mode.NEW;

	}

	@Override
	public String doCreate() {

		log.debug("Creando la encuesta definida...");

		for (DynamicSurveyBlock block : blocks) {
			// Si el bloque es del tipo grilla, cada celda es un detalle.
			if (block instanceof DynamicSurveyDataTable) {
				DynamicSurveyDataTable fields = (DynamicSurveyDataTable) block;
				int numberRow = 1;
				for (DynamicSurveyRow row : fields.getRows()) {
					for (int i = 0; i < row.getCells().length; i++) {
						// Solo almaceno los valores ingresados
						if (!row.getCell(i).getValue().equals("")) {
							EncuestaDetalle detalle = new EncuestaDetalle();
							detalle.setEncuesta(getBean());
							detalle.setNumeroFila(numberRow);
							detalle.setPregunta(block.getQuestion(i + 1));
							detalle.setRespuesta(row.getCell(i).getValue());
							getBean().addDetalle(detalle);
						}
					}
					numberRow++;
				}
			}
			// Si el bloque es del tipo fields, cada field es un detalle.
			if (block instanceof DynamicSurveyFields) {
				DynamicSurveyFields fields = (DynamicSurveyFields) block;
				int index = 1;
				for (DynamicSurveyFieldOption field : fields.getFields()) {
					EncuestaDetalle detalle = new EncuestaDetalle();
					detalle.setEncuesta(getBean());
					detalle.setPregunta(block.getQuestion(index));
					// Solo almaceno los valores ingresados
					if (!field.getField().getValue().equals("")) {

						detalle.setRespuesta(field.getField().getValue());
					}
					// Si permite multiples respuestas, cada respuesta es un
					// detalle
					List<EncuestaDetalleOpcionRespuesta> detallesOpcionRespuesta = new ArrayList<EncuestaDetalleOpcionRespuesta>();

					if (field.getManyOptions() != null) {

						for (OpcionRespuesta opcion : field.getManyOptions()) {
							EncuestaDetalleOpcionRespuesta detalleOpcionRespuesta = new EncuestaDetalleOpcionRespuesta();
							detalleOpcionRespuesta.setOpcionRespuesta(opcion);
							detalleOpcionRespuesta.setEncuestaDetalle(detalle);
							detallesOpcionRespuesta.add(detalleOpcionRespuesta);
							detalle.addDetalleOpcionRespuesta(detalleOpcionRespuesta);
						}

					}
					if (field.getOneOption() != null
							&& field.getOneOption().getDescripcion() != null) {

						EncuestaDetalleOpcionRespuesta detalleOpcionRespuesta = new EncuestaDetalleOpcionRespuesta();
						detalleOpcionRespuesta.setOpcionRespuesta(field
								.getOneOption());
						detalleOpcionRespuesta.setEncuestaDetalle(detalle);
						detallesOpcionRespuesta.add(detalleOpcionRespuesta);
						detalle.addDetalleOpcionRespuesta(detalleOpcionRespuesta);
					}
					if (detalle.getRespuesta() != null
							|| detalle.getOpcionRespuesta() != null) {
						getBean().addDetalle(detalle);
					}
					index++;
				}
			}

		}
		return postCreate();
	}

	public String postCreate() {

		log.debug("postCreate llamado");
		return goUrlSolicitud();
	}

	@Override
	public String doCancel() {

		return goUrlSolicitud();
	}

	public String goNew(EncuestaPlantilla template, Encuesta survey,
			boolean clone) {

		preCreate(template, survey, clone);
		return goUrlSurvey();
	}

	public String goView(EncuestaPlantilla template, Encuesta survey,
			boolean clone) {

		mode = Mode.VIEW;
		return goUrlSurvey();
	}

	public String goDelete(EncuestaPlantilla template, Encuesta survey,
			boolean clone) {

		mode = Mode.DELETE;
		return goUrlSurvey();
	}

	@Override
	public String doDelete() {

		log.debug("Eliminando la encuesta seleccionada..");
		System.out.println("Eliminando el churro con id" + bean.getId());
		if (bean.getId() != null) {
			getBaseLogic().remove(bean);
		}
		return postDelete();
	}

	public String postDelete() {

		log.debug("postDelete llamado..");
		return goUrlSolicitud();
	}

	public abstract String goUrlSurvey();

	public abstract String goUrlSolicitud();

	@Override
	public Encuesta getBean() {

		if (bean == null) {
			bean = getBaseLogic().getNewInstance();
		}
		return bean;
	}

	@Override
	public void setBean(Encuesta bean) {

		this.bean = bean;
	}

	public EncuestaPlantilla getTemplate() {

		return getBean().getPlantilla();
	}

	/**
	 * @return
	 */
	public Encuesta getLastSurvey() {

		return lastSurvey;
	}

	public void setLastSurvey(Encuesta lastSurvey) {

		this.lastSurvey = lastSurvey;
	}

	public Mode getMode() {

		return mode;
	}

	@Override
	public boolean isNew() {

		return mode == Mode.NEW;
	};

	@Override
	public boolean isView() {

		return mode == Mode.VIEW;
	};

	@Override
	public boolean isDelete() {

		return mode == Mode.DELETE;
	};

	public String getValueButtonCancel() {

		if (mode == null) {
			mode = Mode.VIEW;
		}
		String value = "";
		switch (getMode()) {
			case NEW:
				value = "ABM_SURVEY_BUTTON_CANCEL";
				break;
			case VIEW:
				value = "ABM_SURVEY_BUTTON_BACK";
				break;
			case DELETE:
				value = "ABM_SURVEY_BUTTON_CANCEL";
				break;
			default:
				value = "BREADCRUM_UNKNOWN";
				break;
		}
		return I18nHelper.getMessage(value);
	}

	@Override
	public boolean isEditable() {

		if (mode == null) {
			mode = Mode.VIEW;
		}
		switch (mode) {
			case NEW:
				return true;
			case VIEW:
				return false;
			case DELETE:
				return false;
			default:
				break;
		}
		return true;
	}

}
