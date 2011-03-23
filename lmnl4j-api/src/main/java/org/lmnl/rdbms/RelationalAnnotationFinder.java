package org.lmnl.rdbms;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.lmnl.AnnotationFinder;
import org.lmnl.Layer;
import org.lmnl.QName;
import org.lmnl.QNameRepository;
import org.lmnl.Range;

import com.google.common.base.Preconditions;

public class RelationalAnnotationFinder implements AnnotationFinder {

	private SessionFactory sessionFactory;

	private QNameRepository nameRepository;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setNameRepository(QNameRepository nameRepository) {
		this.nameRepository = nameRepository;
	}


	@SuppressWarnings("unchecked")
	public Iterable<Layer> find(Layer layer, QName name, Range range) {
		Preconditions.checkArgument(layer instanceof LayerRelation);

		final Criteria c = sessionFactory.getCurrentSession().createCriteria(LayerRelation.class);
		c.createCriteria("owner").add(Restrictions.idEq(((LayerRelation) layer).getId()));
		c.addOrder(Order.asc("range.start")).addOrder(Order.asc("range.end"));
		
		if (name != null) {
			c.createCriteria("name").add(Restrictions.idEq(((QNameRelation) nameRepository.get(name)).getId()));
		}
		
		if (range != null) {
			c.add(Restrictions.lt("range.start", range.getEnd()));
			c.add(Restrictions.gt("range.end", range.getStart()));
		}
		return c.list();
	}
}
