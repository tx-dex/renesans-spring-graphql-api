package fi.sangre.renesans.persistence.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;

import java.io.Serializable;
import java.util.Optional;

public class UseExistingIdIdentityGenerator extends IdentityGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        final Serializable id = session.getEntityPersister(null, object).getClassMetadata().getIdentifier(object, session);

        return Optional.ofNullable(id)
                .orElseGet(() -> super.generate(session, object));
    }
}
