package org.lmnl.rdbms;

import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.lmnl.AbstractAnnotationRepository;
import org.lmnl.Annotation;
import org.lmnl.QName;
import org.lmnl.QNameRepository;
import org.lmnl.Range;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class RelationalAnnotationRepository extends AbstractAnnotationRepository {

	private SessionFactory sessionFactory;

	private QNameRepository nameRepository;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setNameRepository(QNameRepository nameRepository) {
		this.nameRepository = nameRepository;
	}

	@SuppressWarnings("unchecked")
	public Iterable<Annotation> find(Annotation annotation, Set<QName> names, Set<Range> ranges, boolean overlapping) {
		Preconditions.checkArgument(annotation instanceof AnnotationRelation, annotation.getClass().toString());

		final Criteria c = sessionFactory.getCurrentSession().createCriteria(AnnotationRelation.class);
		c.createCriteria("owner").add(Restrictions.idEq(((AnnotationRelation) annotation).getId()));
		c.addOrder(Order.asc("range.start")).addOrder(Order.asc("range.end"));

		if (names != null && !names.isEmpty()) {
			final Set<Integer> nameIds = Sets.newHashSet();
			for (QName name : nameRepository.get(names)) {
				nameIds.add(((QNameRelation) name).getId());
			}
			c.createCriteria("name").add(Restrictions.in("id", nameIds));
		}

		if (ranges != null && !ranges.isEmpty()) {
			final Disjunction dj = Restrictions.disjunction();
			for (Range range : ranges) {
				final Conjunction cj = Restrictions.conjunction();
				cj.add(Restrictions.lt("range.start", range.getEnd()));
				cj.add(Restrictions.gt("range.end", range.getStart()));
				dj.add(cj);
			}
			c.add(dj);
		}
		return c.list();
	}
}
