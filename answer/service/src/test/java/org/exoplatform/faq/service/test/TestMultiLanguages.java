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
import java.util.Date;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.faq.test.FAQServiceTestCase;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Oct 5, 2012  
 */
public class TestMultiLanguages extends FAQServiceTestCase {
  
  private FAQService    faqService_;

  private FAQSetting    faqSetting_;

  private static String categoryId1;

  private static String questionId1;

  private static String questionPath1;

  public TestMultiLanguages() throws Exception {
    super();
  }
  
  public void setUp() throws Exception {
    //
    super.setUp();
    ConversationState conversionState = ConversationState.getCurrent();
    if(conversionState == null) {
      conversionState = new ConversationState(new Identity("root"));
      ConversationState.setCurrent(conversionState);
    }
    faqService_ = (FAQService) container.getComponentInstanceOfType(FAQService.class);
    
    //
    faqSetting_ = new FAQSetting();
    faqSetting_.setDisplayMode("both");
    faqSetting_.setOrderBy("created");
    faqSetting_.setOrderType("asc");
    faqSetting_.setSortQuestionByVote(true);
    faqSetting_.setIsAdmin("TRUE");
    faqSetting_.setEmailMoveQuestion("content email move question");
    faqSetting_.setEmailSettingSubject("Send notify watched");
    faqSetting_.setEmailSettingContent("Question content: &questionContent_ <br/>Response: &questionResponse_ <br/> link: &questionLink_");
    
    //
    defaultData();
  }
  
  public void tearDown() throws Exception {
    removeData();
  }
  
  public void testAddLanguage() throws Exception {
    //
    List<QuestionLanguage> questionLanguages = faqService_.getQuestionLanguages(questionPath1);
    assertEquals(1, questionLanguages.size());
    
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    questionLanguages = faqService_.getQuestionLanguages(questionPath1);
    assertEquals(3, questionLanguages.size());
  }
  
  public void testDeleteAnswerQuestionLang() throws Exception {
    //
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    QuestionLanguage questionLanguage = faqService_.getQuestionLanguageByLanguage(questionPath1, "VietNam");
    Answer answer = createAnswer("root", "Answer of language VietNam 1");
    String answerId = answer.getId();
    answer.setLanguage("VietNam");
    questionLanguage.setAnswers(new Answer[] { answer });
    faqService_.saveAnswer(questionPath1, questionLanguage);
    assertNotNull(faqService_.getAnswerById(questionPath1, answerId, "VietNam"));
    
    //
    MultiLanguages.deleteAnswerQuestionLang(questionNode, answerId, "VietNam");
    assertNull(faqService_.getAnswerById(questionPath1, answerId, "VietNam"));
  }
  
  public void testDeleteCommentQuestionLang() throws Exception {
    //
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    Comment comment = createComment("root", "New comment of question language");
    String commentId = comment.getId();
    comment.setNew(true);
    faqService_.saveComment(questionPath1, comment, "VietNam");
    assertNotNull(faqService_.getCommentById(questionPath1, commentId, "VietNam"));
    
    //
    MultiLanguages.deleteCommentQuestionLang(questionNode, commentId, "VietNam");
    assertNull(faqService_.getCommentById(questionPath1, commentId, "VietNam"));
  }
  
  public void testGetQuestionLanguageByLanguage() throws Exception {
    //
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    QuestionLanguage questionLanguage = MultiLanguages.getQuestionLanguageByLanguage(questionNode, "VietNam");
    assertNotNull(questionLanguage);
    assertEquals("VietNam", questionLanguage.getLanguage());
  }
  
  public void testGetCommentById() throws Exception {
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    Comment comment = createComment("root", "New comment of question language");
    String commentId = comment.getId();
    comment.setNew(true);
    faqService_.saveComment(questionPath1, comment, "VietNam");
    assertNotNull(MultiLanguages.getCommentById(questionNode, commentId, "VietNam"));
    assertNull(MultiLanguages.getCommentById(questionNode, commentId, "Thailand"));
  }
  
  public void testGetAnswerById() throws Exception {
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    QuestionLanguage questionLanguage = faqService_.getQuestionLanguageByLanguage(questionPath1, "VietNam");
    Answer answer = createAnswer("root", "Answer of language VietNam 1");
    String answerId = answer.getId();
    answer.setLanguage("VietNam");
    questionLanguage.setAnswers(new Answer[] { answer });
    faqService_.saveAnswer(questionPath1, questionLanguage);
    assertNotNull(MultiLanguages.getAnswerById(questionNode, answerId, "VietNam"));
  }
  
  public void testSaveAnswer() throws Exception {
    //
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    QuestionLanguage questionLanguage = faqService_.getQuestionLanguageByLanguage(questionPath1, "VietNam");
    Answer answer = createAnswer("root", "Answer of language VietNam 1");
    String answerId = answer.getId();
    answer.setLanguage("VietNam");
    questionLanguage.setAnswers(new Answer[] { answer });
    assertNull(MultiLanguages.getAnswerById(questionNode, answerId, "VietNam"));
    
    //
    MultiLanguages.saveAnswer(questionNode, questionLanguage);
    assertNotNull(MultiLanguages.getAnswerById(questionNode, answerId, "VietNam"));
  }
  
  public void testSaveComment() throws Exception {
    //
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    Comment comment = createComment("root", "New comment of question language");
    String commentId = comment.getId();
    comment.setNew(true);
    assertNull(MultiLanguages.getCommentById(questionNode, commentId, "VietNam"));
    
    //
    MultiLanguages.saveComment(questionNode, comment, "VietNam");
    assertNotNull(MultiLanguages.getCommentById(questionNode, commentId, "VietNam"));
  }
  
  public void testRemoveLanguage() throws Exception {
    // TODO:
  }
  
  public void testVoteAnswer() throws Exception {
    // TODO:
  }
  
  public void testVoteQuestion() throws Exception {
    // TODO:
  }
  
  public void testUnVoteQuestion() throws Exception {
    // TODO:
  }
  
  private void defaultData() throws Exception {    
    //
    Category cate1 = createCategory("Category 1 to test question", 1);
    categoryId1 = Utils.CATEGORY_HOME + "/" + cate1.getId();
    Category cate2 = createCategory("Category 2 to test question", 2);
    Category cate3 = createCategory("Category 3 has not question", 3);
    cate3.setModerators(new String[] { "demo" });
    
    //
    faqService_.saveCategory(Utils.CATEGORY_HOME, cate1, true);
    faqService_.saveCategory(Utils.CATEGORY_HOME, cate2, true);
    faqService_.saveCategory(Utils.CATEGORY_HOME, cate3, true);
    
    //
    Question question1 = createQuestion(categoryId1);
    questionId1 = question1.getId();
    questionPath1 = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1;
    
    //
    Question question2 = createQuestion(categoryId1);
    question2.setRelations(new String[] {});
    question2.setLanguage("English");
    question2.setAuthor("root");
    question2.setEmail("root@exoplatform.com");
    question2.setDetail("Really?");
    question2.setCreatedDate(new Date());
    
    //
    Question question3 = createQuestion(categoryId1);
    question3.setRelations(new String[] {});
    question3.setLanguage("English");
    question3.setAuthor("Demo gtn");
    question3.setEmail("demo@exoplatform.com");
    question3.setDetail("What does eXo Forum do?");
    question3.setCreatedDate(new Date());
    
    //
    Question question4 = createQuestion(categoryId1);
    question4.setRelations(new String[] {});
    question4.setLanguage("English");
    question4.setAuthor("John Anthony");
    question4.setEmail("john@exoplatform.com");
    question4.setDetail("Tell me why?");
    question4.setCreatedDate(new Date());
    
    //
    Question question5 = createQuestion(categoryId1);
    question5.setRelations(new String[] {});
    question5.setLanguage("English");
    question5.setAuthor("Mary Kelly");
    question5.setEmail("mary@exoplatform.com");
    question5.setDetail("How can I build eXo Forum?");
    question5.setCreatedDate(new Date());

    //
    faqService_.saveQuestion(question1, true, faqSetting_);
    faqService_.saveQuestion(question2, true, faqSetting_);
    faqService_.saveQuestion(question3, true, faqSetting_);
    faqService_.saveQuestion(question4, true, faqSetting_);
    faqService_.saveQuestion(question5, true, faqSetting_);
  }
  
  private void removeData() throws Exception {
    FAQSetting faqSetting = new FAQSetting();
    faqSetting.setIsAdmin("TRUE");
    List<Category> categories = faqService_.getSubCategories(Utils.CATEGORY_HOME, faqSetting, false, null);
    for (Category category : categories) {
      faqService_.removeCategory(category.getPath());
    }
  }
  
  private Category createCategory(String categoryName, int  index) {
    Date date = new Date();
    Category category = new Category();
    category.setName(categoryName);
    category.setDescription("Description");
    category.setModerateQuestions(true);
    category.setModerateAnswers(true);
    category.setViewAuthorInfor(true);
    category.setModerators(new String[] { "root" });
    category.setCreatedDate(date);
    category.setUserPrivate(new String[] { "" });
    category.setIndex(index);
    category.setView(true);
    return category;
  }

  private Question createQuestion(String cateId) throws Exception {
    Question question = new Question();
    question.setLanguage("English");
    question.setQuestion("What is FAQ?");
    question.setDetail("Add new question 1");
    question.setAuthor("root");
    question.setEmail("maivanha1610@gmail.com");
    question.setActivated(true);
    question.setApproved(true);
    question.setCreatedDate(new Date());
    question.setCategoryId(cateId);
    question.setCategoryPath(cateId);
    question.setRelations(new String[] {});
    question.setAttachMent(new ArrayList<FileAttachment>());
    question.setAnswers(new Answer[] {});
    question.setComments(new Comment[] {});
    question.setUsersVote(new String[] {});
    question.setMarkVote(0.0);
    question.setUsersWatch(new String[] {});
    question.setEmailsWatch(new String[] {});
    question.setTopicIdDiscuss(null);
    return question;
  }
  
  private Answer createAnswer(String user, String content) {
    Answer answer = new Answer();
    answer.setActivateAnswers(true);
    answer.setApprovedAnswers(true);
    answer.setDateResponse(new Date());
    answer.setMarksVoteAnswer(0);
    answer.setMarkVotes(0);
    answer.setNew(true);
    answer.setPostId(null);
    answer.setResponseBy(user);
    answer.setResponses(content);
    answer.setUsersVoteAnswer(null);
    answer.setLanguage("English");
    return answer;
  }

  private Comment createComment(String user, String content) {
    Comment comment = new Comment();
    comment.setCommentBy(user);
    comment.setComments(content);
    comment.setDateComment(new Date());
    comment.setNew(true);
    comment.setPostId(null);
    comment.setFullName(user + " " + user);
    return comment;
  }

  private QuestionLanguage createQuestionLanguage(String language) {
    QuestionLanguage questionLanguage = new QuestionLanguage();
    questionLanguage.setAnswers(null);
    questionLanguage.setComments(null);
    questionLanguage.setDetail("detail for language " + language);
    questionLanguage.setLanguage(language);
    questionLanguage.setQuestion("question for language " + language);
    return questionLanguage;
  }
}
