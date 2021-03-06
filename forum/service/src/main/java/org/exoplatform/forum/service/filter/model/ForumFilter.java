/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.service.filter.model;

public class ForumFilter {

  private String forumId;

  private String forumName;

  public ForumFilter() {
  }

  public ForumFilter(String forumId, String forumName) {
    this.forumId = forumId;
    this.forumName = forumName;
  }

  public String getForumId() {
    return forumId;
  }

  public void setForumId(String forumId) {
    this.forumId = forumId;
  }

  public String getForumName() {
    return forumName;
  }

  public void setForumName(String forumName) {
    this.forumName = forumName;
  }
  
  @Override
  public boolean equals(Object forumFilter) {
    if (forumFilter instanceof ForumFilter &&
          ((ForumFilter) forumFilter).getForumId() == this.getForumId()) {
      return true;
    }
    return false;
  }
}
