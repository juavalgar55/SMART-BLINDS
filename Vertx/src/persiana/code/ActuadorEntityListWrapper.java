package persiana.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActuadorEntityListWrapper {
	private List<ActuadorEntity> actuadorList;

	public ActuadorEntityListWrapper() {
		super();
	}

	public ActuadorEntityListWrapper(Collection<ActuadorEntity> actuadorList) {
		super();
		this.actuadorList = new ArrayList<ActuadorEntity>(actuadorList);
	}
	
	public ActuadorEntityListWrapper(List<ActuadorEntity> actuadorList) {
		super();
		this.actuadorList = new ArrayList<ActuadorEntity>(actuadorList);
	}

	public List<ActuadorEntity> getactuadorList() {
		return actuadorList;
	}

	public void setactuadorList(List<ActuadorEntity> actuadorList) {
		this.actuadorList = actuadorList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actuadorList == null) ? 0 : actuadorList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActuadorEntityListWrapper other = (ActuadorEntityListWrapper) obj;
		if (actuadorList == null) {
			if (other.actuadorList != null)
				return false;
		} else if (!actuadorList.equals(other.actuadorList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActuadorEntityListWrapper [actuadorList=" + actuadorList + "]";
	}



}
