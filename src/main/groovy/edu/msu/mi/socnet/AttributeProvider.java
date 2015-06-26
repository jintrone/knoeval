package edu.msu.mi.socnet;

/**
 * Created by josh on 6/9/15.
 */
public interface AttributeProvider {

    MyGraphMLExporter.AttributeType getType();

    String getName();

    String getDataType();

    String getValue(Object obj);


}
