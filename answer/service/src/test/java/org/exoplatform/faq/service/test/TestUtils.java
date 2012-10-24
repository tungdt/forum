/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.service.test;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.test.FAQServiceTestCase;
import org.exoplatform.forum.common.UserHelper;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Oct 5, 2012  
 */
public class TestUtils extends FAQServiceTestCase {
  
  public TestUtils() throws Exception {
    super();
  }

  public void testHasPermission() throws Exception {
    List<String> listUsers = new ArrayList<String>();
    listUsers.add("root");
    listUsers.add("john");
    assertTrue(Utils.hasPermission(UserHelper.getAllGroupAndMembershipOfUser("root"), listUsers));
    assertTrue(Utils.hasPermission(UserHelper.getAllGroupAndMembershipOfUser("john"), listUsers));
    assertFalse(Utils.hasPermission(UserHelper.getAllGroupAndMembershipOfUser("demo"), listUsers));
  }
  
  public void testGetTimeOfLastActivity() throws Exception {
    assertEquals(1315131135, Utils.getTimeOfLastActivity("root-1315131135"));
    assertEquals(-1, Utils.getTimeOfLastActivity("root1315131135"));
    assertEquals(-1, Utils.getTimeOfLastActivity("root-1dsaf315131135"));
  }
  
  public void testGetAuthorOfLastActivity() throws Exception {
    assertEquals("root", Utils.getAuthorOfLastActivity("root-1315131135"));
    assertEquals(null, Utils.getAuthorOfLastActivity("root1315131135"));
    assertEquals("root", Utils.getAuthorOfLastActivity("root-1dsaf315131135"));
  }
  
  public void testGetOderBy() throws Exception {
    FAQSetting faqSetting = new FAQSetting();
    faqSetting.setDisplayMode("both");
    faqSetting.setOrderBy("created");
    faqSetting.setOrderType("asc");
    faqSetting.setSortQuestionByVote(true);
    faqSetting.setIsAdmin("TRUE");
    faqSetting.setEmailMoveQuestion("content email move question");
    faqSetting.setEmailSettingSubject("Send notify watched");
    faqSetting.setEmailSettingContent("Question content: &questionContent_ <br/>Response: &questionResponse_ <br/> link: &questionLink_");
    assertEquals("@exo:markVote descending, @exo:createdDate ascending", Utils.getOderBy(faqSetting));;
  }
  
  public void testBuildQueryListOfUser() throws Exception {
    List<String> users = new ArrayList<String>();
    users.add("root");
    users.add("demo");
    assertEquals("@exo:isAdmin = 'root' or @exo:isAdmin = 'demo'", Utils.buildQueryListOfUser("exo:isAdmin", users));
  }
}
