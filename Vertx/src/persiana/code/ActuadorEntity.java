package persiana.code;


import java.util.Objects;

public class ActuadorEntity {
	protected Integer idActuador;
	protected Integer idPlaca;
	protected Boolean estado;
	protected Integer grados;
	protected Long timeStamp;
	protected Integer idGroup;
	
	public ActuadorEntity(Integer idActuador,Integer idPlaca,Boolean estado,Integer grados,Long timeStamp,Integer idGroup) {
		super();
		this.idActuador = idActuador;
		this.idPlaca = idPlaca;
		this.estado = estado;
		this.grados=grados;
		this.timeStamp=timeStamp;
		this.idGroup=idGroup;

	}

	public Integer getIdActuador() {
		return idActuador;
	}

	public void setIdActuador(Integer idActuador) {
		this.idActuador = idActuador;
	}

	public Integer getIdPlaca() {
		return idPlaca;
	}

	public void setIdPlaca(Integer idPlaca) {
		this.idPlaca = idPlaca;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Integer getGrados() {
		return grados;
	}

	public void setGrados(Integer grados) {
		this.grados = grados;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Integer getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(Integer idGroup) {
		this.idGroup = idGroup;
	}

	@Override
	public int hashCode() {
		return Objects.hash(estado, grados, idActuador, idGroup, idPlaca, timeStamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActuadorEntity other = (ActuadorEntity) obj;
		return Objects.equals(estado, other.estado) && Objects.equals(grados, other.grados)
				&& Objects.equals(idActuador, other.idActuador) && Objects.equals(idGroup, other.idGroup)
				&& Objects.equals(idPlaca, other.idPlaca) && Objects.equals(timeStamp, other.timeStamp);
	}

	@Override
	public String toString() {
		return "ActuadorEntity [idActuador=" + idActuador + ", idPlaca=" + idPlaca + ", estado=" + estado + ", grados="
				+ grados + ", timeStamp=" + timeStamp + ", idGroup=" + idGroup + "]";
	}





}
