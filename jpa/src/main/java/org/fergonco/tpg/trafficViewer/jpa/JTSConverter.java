package org.fergonco.tpg.trafficViewer.jpa;

import java.sql.SQLException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.persistence.PersistenceException;

import org.postgresql.util.PGobject;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

@Converter
public class JTSConverter implements AttributeConverter<Geometry, Object> {

	private WKBWriter writer;
	private WKBReader reader;

	public JTSConverter() {
		writer = new WKBWriter(2, true);
		reader = new WKBReader();
	}

	@Override
	public Object convertToDatabaseColumn(Geometry attribute) {
		PGobject ret = new PGobject();
		ret.setType("geometry");

		try {
			if (attribute == null) {
				ret.setValue(null);
			} else {
				byte[] bytes = writer.write(attribute);
				ret.setValue(WKBWriter.toHex(bytes));
			}
		} catch (SQLException e) {
			throw new PersistenceException("Cannot encode geometry", e);
		}
		return ret;
	}

	@Override
	public Geometry convertToEntityAttribute(Object dbData) {
		PGobject pgObject = (PGobject) dbData;
		if (pgObject == null) {
			return null;
		} else {
			try {
				return reader.read(WKBReader.hexToBytes(pgObject.getValue()));
			} catch (ParseException e) {
				throw new PersistenceException("Could not convert byte[] from database to JTS geometry", e);
			}
		}
	}

}
