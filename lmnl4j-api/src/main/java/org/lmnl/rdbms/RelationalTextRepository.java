package org.lmnl.rdbms;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singleton;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.SortedMap;
import java.util.SortedSet;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.Work;
import org.lmnl.Annotation;
import org.lmnl.Range;
import org.lmnl.Text;
import org.lmnl.TextContentReader;
import org.lmnl.TextRepository;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;

public class RelationalTextRepository implements TextRepository {

	private SessionFactory sessionFactory;
	private String contentColumn = "content";
	private String textRelation = "lmnl_text";

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setContentColumn(String contentColumn) {
		this.contentColumn = contentColumn;
	}

	public void setTextRelation(String textRelation) {
		this.textRelation = textRelation;
	}

	public void read(Annotation annotation, final TextContentReader reader) throws IOException {
		sessionFactory.getCurrentSession().doWork(new TextContentRetrieval<Void>(getText(annotation)) {

			@Override
			protected Void retrieve(Clob content) throws SQLException, IOException {
				Reader contentReader = null;
				try {
					reader.read(contentReader = content.getCharacterStream(), (int) content.length());
				} catch (IOException e) {
					Throwables.propagate(e);
				} finally {
					Closeables.close(contentReader, false);
				}
				return null;
			}
		});
	}

	public String read(Annotation annotation, Range range) throws IOException {
		return getOnlyElement(bulkRead(annotation, Sets.newTreeSet(singleton(range))).values());
	}

	public int length(Annotation annotation) throws IOException {
		final TextContentRetrieval<Integer> contentLengthRetrieval = new TextContentRetrieval<Integer>(getText(annotation)) {

			@Override
			protected Integer retrieve(Clob content) throws SQLException, IOException {
				return (int) content.length();
			}
		};

		sessionFactory.getCurrentSession().doWork(contentLengthRetrieval);
		return contentLengthRetrieval.returnValue;
	}

	public SortedMap<Range, String> bulkRead(Annotation annotation, final SortedSet<Range> ranges) throws IOException {
		final SortedMap<Range, String> results = Maps.newTreeMap();
		sessionFactory.getCurrentSession().doWork(new TextContentRetrieval<Void>(getText(annotation)) {

			@Override
			protected Void retrieve(Clob content) throws SQLException, IOException {
				for (Range range : ranges) {
					results.put(range, content.getSubString(range.getStart() + 1, range.length()));
				}
				return null;
			}
		});
		return results;
	}

	public void write(final Annotation annotation, final Reader contents, final int contentLength) throws IOException {
		sessionFactory.getCurrentSession().doWork(new Work() {

			public void execute(Connection connection) throws SQLException {
				final PreparedStatement updateStmt = connection.prepareStatement("UPDATE " + textRelation + " SET "
						+ contentColumn + " = ? WHERE id = ?");
				try {
					updateStmt.setClob(1, contents, contentLength);
					updateStmt.setInt(2, getText(annotation).getId());
					updateStmt.executeUpdate();
				} finally {
					updateStmt.close();
				}

			}
		});
	}

	TextRelation getText(Annotation annotation) {
		Preconditions.checkArgument(annotation instanceof AnnotationRelation);
		final Session session = sessionFactory.getCurrentSession();
		
		Criteria c = session.createCriteria(AnnotationRelation.class);
		c.add(Restrictions.idEq(((AnnotationRelation) annotation).getId()));
		c.setFetchMode("text", FetchMode.JOIN);

		return Preconditions.checkNotNull((AnnotationRelation) c.uniqueResult()).getText();
	}

	private abstract class TextContentRetrieval<T> implements Work {
		private final Text text;
		private T returnValue;

		public TextContentRetrieval(Text text) {
			this.text = text;
		}

		public void execute(Connection connection) throws SQLException {
			final PreparedStatement contentStmt = connection.prepareStatement("SELECT " + contentColumn + " FROM "
					+ textRelation + " WHERE id = ?");
			try {
				contentStmt.setInt(1, ((TextRelation) text).getId());
				final ResultSet resultSet = contentStmt.executeQuery();
				try {
					if (resultSet.next()) {
						returnValue = retrieve(resultSet.getClob(contentColumn));
					}
				} finally {
					resultSet.close();
				}
			} catch (IOException e) {
				Throwables.propagate(e);
			} finally {
				contentStmt.close();
			}
		}

		protected abstract T retrieve(Clob content) throws SQLException, IOException;
	}
}
