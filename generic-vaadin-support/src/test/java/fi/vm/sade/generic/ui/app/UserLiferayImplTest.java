package fi.vm.sade.generic.ui.app;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;

import junit.framework.Assert;

import org.junit.Test;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.util.Portal;
import com.liferay.portal.util.PortalUtil;
public class UserLiferayImplTest {

    @Test
    public void test() throws PortalException, SystemException {
        
        final PortletRequest req = createMock(PortletRequest.class);
        final PortalUtil pu = new PortalUtil();
        final Portal portal = createMock(Portal.class);
        
        com.liferay.portal.model.User user = createMock(com.liferay.portal.model.User.class);
        List<UserGroup> portalGroups = new ArrayList<UserGroup>();
        expect(user.getUserGroups()).andReturn(portalGroups);
        expect(user.getEmailAddress()).andReturn("foo@bar").anyTimes();
        
        portalGroups.add(createGroup("APP_ORGANISAATIOHALLINTA_CRUD_123456"));
        portalGroups.add(createGroup("APP_ORGANISAATIOHALLINTA_READ_UPDATE_123457"));
        portalGroups.add(createGroup("APP_ORGANISAATIOHALLINTA_READ_123456"));
        
        expect(portal.getUser(req)).andReturn(user).anyTimes();
        
        pu.setPortal(portal);
        
        replay(req);
        replay(portal);
        replay(user);
        UserLiferayImpl theUser = new UserLiferayImpl(req);
        
        ///XXX feilaa ilman fiksiä:
        Assert.assertEquals(2, theUser.getOrganisations().size());
    }

    private UserGroup createGroup(String groupName) {
        final UserGroup group = createMock(UserGroup.class);
        expect(group.getName()).andReturn(groupName);
        replay(group);
        return group;
    }

}
