package persiana.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SensorEntityListWrapper {
	private List<SensorEntity> sensorList;

	public SensorEntityListWrapper() {
		super();
	}

	public SensorEntityListWrapper(Collection<SensorEntity> sensorList) {
		super();
		this.sensorList = new ArrayList<SensorEntity>(sensorList);
	}
	
	public SensorEntityListWrapper(List<SensorEntity> sensorList) {
		super();
		this.sensorList = new ArrayList<SensorEntity>(sensorList);
	}

	public List<SensorEntity> getsensorList() {
		return sensorList;
	}

	public void setsensorList(List<SensorEntity> sensorList) {
		this.sensorList = sensorList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sensorList == null) ? 0 : sensorList.hashCode());
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
		SensorEntityListWrapper other = (SensorEntityListWrapper) obj;
		if (sensorList == null) {
			if (other.sensorList != null)
				return false;
		} else if (!sensorList.equals(other.sensorList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SensorEntityListWrapper [sensorList=" + sensorList + "]";
	}


}
