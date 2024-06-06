package persiana.code;

import java.util.Objects;

public class Actuador {

	Integer actuadorId;
	String actuadorPass;

	
	public Actuador(Integer actuadorId,String actuadorPass) {
		super();
		
		this.actuadorId = actuadorId;
		this.actuadorPass = actuadorPass;

	}


	public Integer getActuadorId() {
		return actuadorId;
	}


	public void setActuadorId(Integer actuadorId) {
		this.actuadorId = actuadorId;
	}


	public String getActuadorPass() {
		return actuadorPass;
	}


	public void setActuadorPass(String actuadorPass) {
		this.actuadorPass = actuadorPass;
	}


	@Override
	public int hashCode() {
		return Objects.hash(actuadorId, actuadorPass);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Actuador other = (Actuador) obj;
		return Objects.equals(actuadorId, other.actuadorId) && Objects.equals(actuadorPass, other.actuadorPass);
	}


	@Override
	public String toString() {
		return "Actuador [actuadorId=" + actuadorId + ", actuadorPass=" + actuadorPass + "]";
	}


}
