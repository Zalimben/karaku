/*
 * @DynamicSurveyField.java 1.0 05/06/2013 Sistema Integral de Gestion
 * Hospitalaria
 */
package py.una.med.base.survey.components;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import py.una.med.base.survey.domain.EncuestaPlantillaPregunta;

/**
 * Esta clase representa un Field, o mejor dicho un InputText con validaciones
 * de required y size MAX.
 * 
 * @author Nathalia Ochoa
 * @since 1.0
 * @version 1.0 05/06/2013
 * 
 */
public class DynamicSurveyField {

	public static interface SurveyField {

		public String getId();

		public void setId(String id);

		public String getValue();

		public void setValue(String value);

		public int getIndex();

		public void setIndex(int index);

		public int getMax();
	}

	public static class NotRequired implements SurveyField {

		String value = "";
		int index;
		String id;

		private final int max;
		boolean validate;

		@AssertTrue(message = "Se ha exedido la longitud maxima")
		public boolean isValidate() {

			if (value.length() > max) {
				return false;
			}
			return true;
		}

		public NotRequired(int index, int max) {

			super();
			this.max = max;
			this.index = index;
			this.id = "_cell_" + String.valueOf(index);
		}

		@Override
		public String getValue() {

			return value;
		}

		@Override
		public void setValue(String value) {

			this.value = value;
		}

		@Override
		public int getIndex() {

			return this.index;
		}

		@Override
		public void setIndex(int index) {

			this.index = index;
		}

		@Override
		public String getId() {

			return this.id;
		}

		@Override
		public void setId(String id) {

			this.id = id;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see py.una.med.base.survey.DynamicSurveyField.SurveyField#getMax()
		 */
		@Override
		public int getMax() {

			return this.max;

		}

	}

	public static class Required implements SurveyField {

		@NotNull
		String value = "";
		int index;
		String id;

		private final int max;
		boolean validate;

		@AssertTrue(message = "Se ha exedido la longitud maxima")
		public boolean isValidate() {

			if (value.length() > max) {

				return false;
			}
			return true;
		}

		public Required(int index, int max) {

			super();
			this.index = index;
			this.max = max;
			this.id = "_cell_" + String.valueOf(index);
		}

		@Override
		public String getValue() {

			return value;
		}

		@Override
		public void setValue(String value) {

			this.value = value;
		}

		@Override
		public int getIndex() {

			return this.index;
		}

		@Override
		public void setIndex(int index) {

			this.index = index;
		}

		@Override
		public String getId() {

			return this.id;
		}

		@Override
		public void setId(String id) {

			this.id = id;

		}

		@Override
		public int getMax() {

			return this.max;

		}

	}

	/**
	 * Construye un Field a ser utilizado dentro del formulario
	 * 
	 * @param columna
	 *            Field a representar
	 * @return Field requerido o no de acuerdo a si es obligatoria o no.
	 */
	public static SurveyField fieldFactory(EncuestaPlantillaPregunta columna) {

		if (columna.getObligatoria().equals("SI")) {
			return new DynamicSurveyField.Required(columna.getOrden(),
					columna.getLongitudRespuesta());

		} else {
			return new DynamicSurveyField.NotRequired(columna.getOrden(),
					columna.getLongitudRespuesta());
		}
	}
}
