package org.lmnl.rdbms;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.lmnl.Layer;
import org.lmnl.Range;
import org.lmnl.Text;
import org.lmnl.TextRepository;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class PersistingTextRepository implements TextRepository {

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Text setText(Layer layer, Reader reader) throws IOException {
		Preconditions.checkArgument(layer instanceof PersistentLayer);
		
		final PersistentText text = getPersistentText(layer);
		final Session session = sessionFactory.getCurrentSession();

		text.setContent(Hibernate.createClob(reader, -1));
		session.update(text);
		session.flush();
		session.refresh(text);

		return text;
	}

	public Reader getText(Layer layer) throws IOException {
		try {
			return getPersistentText(layer).getContent().getCharacterStream();
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}

	public String getText(Layer layer, Range range) throws IOException {
		try {
			return getPersistentText(layer).getContent().getSubString(range.getStart() + 1, range.length());
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}

	protected PersistentText getPersistentText(Layer layer) {
		Preconditions.checkArgument(layer instanceof PersistentLayer);

		Criteria c = sessionFactory.getCurrentSession().createCriteria(Layer.class);
		c.add(Restrictions.idEq(((PersistentLayer) layer).getId()));
		c.setFetchMode("text", FetchMode.JOIN);

		return Preconditions.checkNotNull((PersistentLayer) c.uniqueResult()).getText();
	}
}
