package persiana.code;

import java.util.Objects;

public class Sensor {

	
	Integer sensorId;
	String sensorPass;

	
	public Sensor(Integer sensorId,String sensorPass) {
		super();
		
		this.sensorId = sensorId;
		this.sensorPass = sensorPass;

	}


	public Integer getSensorId() {
		return sensorId;
	}


	public void setSensorId(Integer sensorId) {
		this.sensorId = sensorId;
	}


	public String getSensorPass() {
		return sensorPass;
	}


	public void setSensorPass(String sensorPass) {
		this.sensorPass = sensorPass;
	}


	@Override
	public int hashCode() {
		return Objects.hash(sensorId, sensorPass);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sensor other = (Sensor) obj;
		return Objects.equals(sensorId, other.sensorId) && Objects.equals(sensorPass, other.sensorPass);
	}

	

	
	
}
