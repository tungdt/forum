/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.answer.webui.popup;

import org.exoplatform.answer.rendering.RenderHelper;
import org.exoplatform.answer.webui.BaseUIFAQForm;
import org.exoplatform.answer.webui.FAQUtils;
import org.exoplatform.answer.webui.UIAnswersContainer;
import org.exoplatform.answer.webui.UIAnswersPortlet;
import org.exoplatform.answer.webui.UIQuestions;
import org.exoplatform.answer.webui.ValidatorDataInput;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.forum.common.CommonUtils;
import org.exoplatform.forum.common.webui.BaseEventListener;
import org.exoplatform.forum.common.webui.WebUIUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/answer/webui/popup/UICommentForm.gtmpl", 
    events = {
        @EventConfig(listeners = UICommentForm.SaveActionListener.class), 
        @EventConfig(listeners = UICommentForm.CancelActionListener.class) 
    }
)
public class UICommentForm extends BaseUIFAQForm implements UIPopupComponent {
  private String       languageSelected;

  private Question     question_;

  private Comment      comment;

  private String       questionContent;

  private String       questionDetail;

  private String       currentUser_    = "";

  private String       currentUserDisplayName    = "";

  private final String COMMENT_CONTENT = "CommentContent";

  private FAQSetting   faqSetting_;

  private RenderHelper renderHelper    = new RenderHelper();

  public UICommentForm() throws Exception {
    currentUser_ = FAQUtils.getCurrentUser();
    UIFormWYSIWYGInput commentContent = new UIFormWYSIWYGInput(COMMENT_CONTENT, COMMENT_CONTENT, "");
    commentContent.setFCKConfig(WebUIUtils.getFCKConfig());
    commentContent.setHeight("250px");
    commentContent.setToolBarName("Basic");
    this.addChild(commentContent);
    User user = FAQUtils.getCurrentUserObject();
    if(user != null) {
	String fullName = FAQUtils.getFullName(user.getUserName());
    if (fullName == null || fullName.length() == 0)
      this.currentUserDisplayName = user.getUserName();
    else
      this.currentUserDisplayName = fullName;
    }
  }

  public String getCurrentUserDisplayName() {
    return currentUserDisplayName;
  }

  public void setCurrentUserDisplayName(String currentUserDisplayName) {
    this.currentUserDisplayName = currentUserDisplayName;
  }

  public String getQuestionContent() {
    return questionContent;
  }

  protected String getQuestionDetail() {
    Question question = new Question();
    question.setDetail(questionDetail);
    return renderHelper.renderQuestion(question);
  }

  public void activate() {
  }

  public void deActivate() {
  }

  public void setInfor(Question question, String commentId, FAQSetting faqSetting, String language) throws Exception {
    if (!language.equals(question.getLanguage())) {
      try {
        QuestionLanguage questionLanguage = getFAQService().getQuestionLanguageByLanguage(question.getPath(), language);
        this.questionContent = questionLanguage.getQuestion();
        this.questionDetail = questionLanguage.getDetail();
        languageSelected = language;
      } catch (Exception e) {
        this.questionContent = question.getQuestion();
        this.questionDetail = question.getDetail();
        languageSelected = question.getLanguage();
      }
    } else {
      this.questionContent = question.getQuestion();
      this.questionDetail = question.getDetail();
      languageSelected = question.getLanguage();
    }
    this.question_ = question;
    this.faqSetting_ = faqSetting;
    FAQUtils.getEmailSetting(faqSetting_, false, false);
    if (commentId.indexOf("new") < 0) {
      comment = getFAQService().getCommentById(question.getPath(), commentId, language);
      ((UIFormWYSIWYGInput) this.getChildById(COMMENT_CONTENT)).setValue(CommonUtils.decodeSpecialCharToHTMLnumberIgnore(comment.getComments()));
    }
  }

  static public class CancelActionListener extends EventListener<UICommentForm> {
    public void execute(Event<UICommentForm> event) throws Exception {
      UICommentForm commentForm = event.getSource();
      UIAnswersPortlet portlet = commentForm.getAncestorOfType(UIAnswersPortlet.class);
      portlet.cancelAction();
    }
  }

  static public class SaveActionListener extends BaseEventListener<UICommentForm> {
    public void onEvent(Event<UICommentForm> event, UICommentForm commentForm, final String objectId) throws Exception {
      String comment = ((UIFormWYSIWYGInput) commentForm.getChildById(commentForm.COMMENT_CONTENT)).getValue();
      if (CommonUtils.isEmpty(comment) || !ValidatorDataInput.fckContentIsNotEmpty(comment)) {
        warning("UICommentForm.msg.comment-is-null");
        return;
      }
      if (!commentForm.getFAQService().isExisting(commentForm.question_.getPath())) {
        warning("UIQuestions.msg.comment-id-deleted");
        return;
      }
      UIAnswersPortlet portlet = commentForm.getAncestorOfType(UIAnswersPortlet.class);
      UIAnswersContainer answersContainer = portlet.getChild(UIAnswersContainer.class);
      UIQuestions questions = answersContainer.getChild(UIQuestions.class);
      comment = CommonUtils.encodeSpecialCharInSearchTerm(comment);
      try {
        // Create link by Vu Duy Tu.
        if(FAQUtils.isFieldEmpty(commentForm.question_.getLink())) {
          commentForm.question_.setLink(FAQUtils.getQuestionURI(commentForm.question_.getId(), false));
        }
        if (commentForm.comment != null) {
          commentForm.comment.setNew(false);
        } else {
          commentForm.comment = new Comment();
          commentForm.comment.setNew(true);
        }
        commentForm.comment.setComments(comment);
        // For discuss in forum
        String topicId = commentForm.question_.getTopicIdDiscuss();
        if (topicId != null && topicId.length() > 0) {
          ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
          Topic topic = (Topic) forumService.getObjectNameById(topicId, org.exoplatform.forum.service.Utils.TOPIC);
          if (topic != null) {
            String remoteAddr = WebUIUtils.getRemoteIP();
            String[] ids = topic.getPath().split("/");
            int t = ids.length;
            String linkForum = FAQUtils.getLinkDiscuss(topicId);
            String postId = commentForm.comment.getPostId();
            if (postId == null || postId.length() == 0) {
              Post post = new Post();
              post.setOwner(commentForm.currentUser_);
              post.setIcon("ViewIcon");
              post.setName("Re: " + commentForm.question_.getQuestion());
              post.setMessage(comment);
              post.setLink(linkForum+"/"+postId);
              post.setIsApproved(!topic.getIsModeratePost());
              post.setRemoteAddr(remoteAddr);
              try {
                forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, true, new MessageBuilder());
              } catch (Exception e) {
                event.getSource().log.debug("Saving post fail: ", e);
              }
              commentForm.comment.setPostId(post.getId());
            } else {
              try {
                Post post = forumService.getPost(ids[t - 3], ids[t - 2], topicId, postId);
                boolean isNew = false;
                if (post == null) {
                  post = new Post();
                  isNew = true;
                  post.setOwner(commentForm.currentUser_);
                  post.setIcon("ViewIcon");
                  post.setName("Re: " + commentForm.question_.getQuestion());
                  commentForm.comment.setPostId(post.getId());
                  post.setLink(linkForum+"/"+postId);
                  post.setRemoteAddr(remoteAddr);
                } else {
                  post.setModifiedBy(commentForm.currentUser_);
                }
                post.setIsApproved(!topic.getIsModeratePost());
                post.setMessage(comment);
                forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, isNew, new MessageBuilder());
              } catch (Exception e) {
                event.getSource().log.debug("Saving post fail: ", e);
              }
            }
          }
        }

        String language = "";
        if (!commentForm.languageSelected.equals(commentForm.question_.getLanguage()))
          language = commentForm.languageSelected;
        String currentUser = FAQUtils.getCurrentUser() ;
        commentForm.comment.setCommentBy(currentUser);
        commentForm.comment.setFullName(FAQUtils.getFullName(null)) ;
        commentForm.getFAQService().saveComment(commentForm.question_.getPath(), commentForm.comment, language);
        if (!commentForm.languageSelected.equals(commentForm.question_.getLanguage())) {
          try {
            questions.updateCurrentLanguage();
          } catch (Exception e) {
            questions.updateQuestionLanguageByLanguage(commentForm.question_.getPath(), commentForm.languageSelected);
          }
        } else {
          questions.updateQuestionLanguageByLanguage(commentForm.question_.getPath(), commentForm.languageSelected);
        }
      } catch (Exception e) {
        event.getSource().log.error("Fail to save action: ", e);
        warning("UIQuestions.msg.category-id-deleted", false);
      }
      portlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(answersContainer);
    }
  }
}
