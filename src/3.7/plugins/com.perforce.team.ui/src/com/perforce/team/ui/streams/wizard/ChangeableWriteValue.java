package com.perforce.team.ui.streams.wizard;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;

public class ChangeableWriteValue extends WritableValue{
	private Object valueStub;

	public ChangeableWriteValue() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ChangeableWriteValue(Object initialValue, Object valueType) {
		super(initialValue, valueType);
		// TODO Auto-generated constructor stub
	}

	public ChangeableWriteValue(Realm realm, Object initialValue,
			Object valueType) {
		super(realm, initialValue, valueType);
		// TODO Auto-generated constructor stub
	}

	public ChangeableWriteValue(Realm realm) {
		super(realm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doSetValue(Object value) {
		this.valueStub=value;
		super.doSetValue(valueStub);
	}
	
	@Override
	public Object doGetValue() {
		return valueStub;
	}
	
	public void setValueSilently(Object value){
		this.valueStub=value;
	}
}