package org.lmnl.rdbms;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singleton;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.SortedMap;
import java.util.SortedSet;

import org.lmnl.Annotation;
import org.lmnl.Range;
import org.lmnl.TextRepository;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class RelationalTextRepository implements TextRepository {

	private RelationalAnnotationFactory annotationFactory;
	
	public void setAnnotationFactory(RelationalAnnotationFactory annotationFactory) {
		this.annotationFactory = annotationFactory;
	}
	
	public Reader read(Annotation annotation) throws IOException {
		try {
			return annotationFactory.getText(annotation).getContent().getCharacterStream();
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}

	public String read(Annotation annotation, Range range) throws IOException {
		return getOnlyElement(bulkRead(annotation, Sets.newTreeSet(singleton(range))).values());
	}

	public int length(Annotation annotation) throws IOException {
		try {
			return (int) annotationFactory.getText(annotation).getContent().length();
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}

	public SortedMap<Range, String> bulkRead(Annotation annotation, SortedSet<Range> ranges) throws IOException {
		try {
			final Clob text = annotationFactory.getText(annotation).getContent();
			final SortedMap<Range, String> results = Maps.newTreeMap();
			for (Range range : ranges) {
				results.put(range, text.getSubString(range.getStart() + 1, range.length()));
			}
			return results;
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}
}
