package persiana.code;


import java.util.Objects;

public class SensorEntity {
	protected Integer idSensor;
	protected Integer idPlaca;
	protected Long timeStamp;
	protected Integer temperatura;
	protected Integer idGroup;
	
	
	public SensorEntity(Integer idSensor,Integer idPlaca,Long timeStamp,Integer temperatura,Integer idGroup) {
		super();
		this.idSensor = idSensor;
		this.idPlaca=idPlaca;
		this.timeStamp=timeStamp;
		this.temperatura=temperatura;
		this.idGroup=idGroup;
	}


	public Integer getIdSensor() {
		return idSensor;
	}


	public void setIdSensor(Integer idSensor) {
		this.idSensor = idSensor;
	}


	public Integer getIdPlaca() {
		return idPlaca;
	}


	public void setIdPlaca(Integer idPlaca) {
		this.idPlaca = idPlaca;
	}



	public Long getTimeStamp() {
		return timeStamp;
	}


	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}


	public Integer getTemperatura() {
		return temperatura;
	}


	public void setTemperatura(Integer temperatura) {
		this.temperatura = temperatura;
	}


	public Integer getIdGroup() {
		return idGroup;
	}


	public void setIdGroup(Integer idGroup) {
		this.idGroup = idGroup;
	}


	@Override
	public int hashCode() {
		return Objects.hash(idGroup, idPlaca, idSensor, temperatura, timeStamp);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorEntity other = (SensorEntity) obj;
		return Objects.equals(idGroup, other.idGroup) && Objects.equals(idPlaca, other.idPlaca)
				&& Objects.equals(idSensor, other.idSensor)&& Objects.equals(temperatura, other.temperatura)
				&& Objects.equals(timeStamp, other.timeStamp);
	}


	@Override
	public String toString() {
		return "SensorEntity [idSensor=" + idSensor + ", idPlaca=" + idPlaca + ", tiempo="
				+ timeStamp + ", temperatura=" + temperatura + ", idGroup=" + idGroup + "]";
	}







}
