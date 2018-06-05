package com.perforce.team.core;

import java.util.ArrayList;
import java.util.List;

public class P4JavaEnumHelper {

	public static <T> List<T> filterUnknownValues(T[] values){
		List<T> list=new ArrayList<T>();
		for(T value: values){
			if(!"UNKNOWN".equalsIgnoreCase(value.toString())){
				list.add(value);
			}
		}
		return list;
	}
}
