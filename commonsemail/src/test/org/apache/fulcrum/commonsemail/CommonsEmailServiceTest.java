package org.apache.fulcrum.commonsemail;

/*
 * Copyright 2004, IT20ONE GmbH, Vienna, AUSTRIA
 * All rights reserved.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.fulcrum.testcontainer.BaseUnitTest;


/**
 * CommonsEmailServiceTest
 *
 * @author <a href="mailto:siegfried.goeschl@it20one.at">Siegfried Goeschl</a>
 */
public class CommonsEmailServiceTest extends BaseUnitTest
{
    /** the service to test */
    private CommonsEmailService service;
    
    /** the default subject */
    private String subject;
    
    /** the default domain */
    private String domain;
    
    /** the recipient of the email */
    private String mailTo;

    /** the sender of the email */
    private String mailFrom;

    /** the generated MimeMessage */
    private MimeMessage result;
    
    /** default plain text content */
    private static final String PLAIN_CONTENT = "Hello World";
    
    /** default HTML text content */
    private static final String HTML_CONTENT = "<h1>Hello World</h1>";

    /**
     * Defines the testcase name for JUnit.
     *
     * @param name the testcase's name.
     */
    public CommonsEmailServiceTest(String name)
    {
        super(name);
    }

    /**
     * Test setup
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.domain 	= "test";
        this.subject 	= this.getName();
        this.mailFrom 	= "demo@it20one.at";
        this.mailTo 	= "demo@it20one.at";

        try
        {
            service = (CommonsEmailService) this.lookup(CommonsEmailService.class.getName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown()
    {
        if( this.result != null )
        {            
            try
            {
                File resultFile = new File( new File("temp"), this.getName()+".eml" );
                FileOutputStream fos = new FileOutputStream(resultFile);
                this.result.writeTo(fos);
                fos.flush();
                fos.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }            
        }
        
        super.tearDown();
    }
    
    /**
     * Add all of our test suites
     */
    public static Test suite()
    {
        TestSuite suite= new TestSuite();
        
        suite.addTest( new CommonsEmailServiceTest("testDefaultDomain") );      
        suite.addTest( new CommonsEmailServiceTest("testDerivedDomain") );      
        suite.addTest( new CommonsEmailServiceTest("testHtmlEmail") );      
        suite.addTest( new CommonsEmailServiceTest("testHtmlEmailWithHashtable") );      
        suite.addTest( new CommonsEmailServiceTest("testMultiPartEmail") );      
        suite.addTest( new CommonsEmailServiceTest("testSendEmailToUnknownServer") );      
        suite.addTest( new CommonsEmailServiceTest("testSendMimeMessage") );      
        suite.addTest( new CommonsEmailServiceTest("testSimpleEmail") );      
        suite.addTest( new CommonsEmailServiceTest("testSimpleEmailWithHashtable") );      
        suite.addTest( new CommonsEmailServiceTest("testCreateMimeMessageWithSession") );      
        
        return suite;
    }

    /**
     * @return the CommonsEmailService service to be used
     */
    protected CommonsEmailService getService()
    {
        return this.service;
    }
    
    /**
     * @return Returns the mail subject.
     */
    protected String getSubject()
    {
        return subject;
    }
        
    /**
     * @return Returns the domain name.
     */
    protected String getDomain()
    {
        return domain;
    }
    
    /**
     * @return Returns the mailTo.
     */
    protected String getMailTo()
    {
        return mailTo;
    }
    
    /**
     * @return Returns the mailFrom.
     */
    protected String getMailFrom()
    {
        return mailFrom;
    }
    
    /**
     * @return a preconfigured attachment
     */
    protected EmailAttachment getEmailAttachment()
    {
        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath("./src/test/TestComponentConfig.xml");
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setName("TestComponentConfig.xml");
        attachment.setDescription("TestComponentConfig.xml");
        
        return attachment;        
    }
    /////////////////////////////////////////////////////////////////////////
    // Start of unit tests
    /////////////////////////////////////////////////////////////////////////

    /**
     * Create a simple email and send it. 
     */
    public void testSimpleEmail() throws Exception
    {
        SimpleEmail email = this.getService().createSimpleEmail(this.getDomain());
        
        email.setSubject(this.getSubject());        
        email.setMsg(PLAIN_CONTENT);
        email.addTo(this.getMailTo());
        
        this. result = this.getService().send(this.getDomain(),email);
    }

    /**
     * Create a HTML email and send it. 
     */
    public void testHtmlEmail() throws Exception
    {
        HtmlEmail email = this.getService().createHtmlEmail(this.getDomain());
        
        email.setSubject(this.getSubject());        
        email.setTextMsg(PLAIN_CONTENT);
        email.setHtmlMsg(HTML_CONTENT);        
        email.addTo(this.getMailTo());
        
        this.result = this.getService().send(this.getDomain(),email);                
    }
    
    /**
     * Create a MultiPart email and send it. 
     */
    public void testMultiPartEmail() throws Exception
    {
        MultiPartEmail email = this.getService().createMultiPartEmail(this.getDomain());                       
        EmailAttachment attachment = this.getEmailAttachment();
        
        email.setSubject(this.getSubject());
        email.attach(attachment);                
        email.addTo(this.getMailTo());
        email.setMsg(PLAIN_CONTENT);

        this.result = this.getService().send(this.getDomain(),email);
    }           
    
    /**
     * Use an undefined domain therefore reverting to the default domain.
     * 
     * @throws Exception
     */
    public void testDefaultDomain() throws Exception
    {
        SimpleEmail email = this.getService().createSimpleEmail("grmpff");
        
        email.setSubject(this.getSubject());        
        email.setMsg(PLAIN_CONTENT);
        email.addTo(this.getMailTo());
        
        this.result = this.getService().send(this.getDomain(),email);       
    }
    
    /**
     * We pass "demo@it20one.at" therefore we should get the "it20one.at" domain
     * 
     * @throws Exception
     */
    public void testDerivedDomain() throws Exception
    {
        SimpleEmail email = this.getService().createSimpleEmail("demo@it20one.at");
        
        email.setFrom(this.getMailFrom());
        email.setSubject(this.getSubject());        
        email.setMsg(PLAIN_CONTENT);
        email.addTo(this.getMailTo());
        
        this.result = this.getService().send(email);       
    }
    
    /**
     * Create a HTML email using a Hashtable as input.
     * 
     * @throws Exception
     */
    public void testHtmlEmailWithHashtable() throws Exception
    {
        Vector attachments = new Vector();
        EmailAttachment attachment = this.getEmailAttachment();
        attachments.add(attachment);
        attachments.add(attachment);

        Hashtable content = new Hashtable();
        content.put(Email.EMAIL_SUBJECT, this.getSubject());
        content.put(Email.EMAIL_BODY, HTML_CONTENT);
        content.put(Email.SENDER_EMAIL, this.getMailFrom());
        content.put(Email.RECEIVER_EMAIL, this.getMailTo());
        content.put(Email.ATTACHMENTS, attachments);
        
        HtmlEmail email = this.getService().createHtmlEmail(
            this.getMailFrom(),
            content
            );
        
        this.result = this.getService().send(email);       
    }
    
    /**
     * Create a simple email using a Hashtable as input.
     * 
     * @throws Exception
     */
    public void testSimpleEmailWithHashtable() throws Exception
    {
        Hashtable content = new Hashtable();
        content.put(Email.EMAIL_SUBJECT, this.getSubject());
        content.put(Email.EMAIL_BODY, PLAIN_CONTENT);
        content.put(Email.SENDER_EMAIL, this.getMailFrom());
        content.put(Email.RECEIVER_EMAIL, this.getMailTo());
        
        SimpleEmail email = this.getService().createSimpleEmail(
            this.getMailFrom(),
            content
            );
        
        this.result = this.getService().send(email);       
    }    
    
    /**
     * Create an email and send it to a bogus mailserver
     * resulting in an EmailException. For this test we
     * overwrite the SMTP server and port taken from the
     * domain configuration.
     */
    public void testSendEmailToUnknownServer() throws Exception
    {
        Hashtable content = new Hashtable();
        content.put(Email.EMAIL_SUBJECT, this.getSubject());
        content.put(Email.EMAIL_BODY, PLAIN_CONTENT);
        content.put(Email.SENDER_EMAIL, this.getMailFrom());
        content.put(Email.RECEIVER_EMAIL, this.getMailTo());
        content.put(Email.MAIL_HOST, "localhost");
        content.put(Email.MAIL_PORT, "63178");
        
        SimpleEmail email = this.getService().createSimpleEmail(
            this.getMailFrom(),
            content
            );
        
        try
        {
            this.result = this.getService().send(email);
            
            if( this.getService().isMailDoNotSend(email.getFromAddress().getAddress()) == false )
            {
                fail();
            }
        }
        catch( EmailException e )
        {
            // expected
        }
    }
    
    /**
     * Create a mail session and simple MimeMessage and sent it 
     * @throws Exception the test failed
     */
    public void testCreateMimeMessageWithSession() throws Exception
    {
        MimeMessage mimeMessage = null;
        Session session = this.getService().createSmtpSession("test","foo","bar");
        
        mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(this.mailFrom));
        mimeMessage.setSubject(this.getSubject());
        mimeMessage.setText(PLAIN_CONTENT);
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(this.mailTo));

        this. result = this.getService().send(
            session,
            mimeMessage
            );
    }
    /**
     * Use commons-email to build a MimeMessage and send it directly
     * 
     * @throws Exception
     */
    public void testSendMimeMessage() throws Exception
    {
        MimeMessage mimeMessage = null;
        SimpleEmail email = this.getService().createSimpleEmail(this.getDomain());
        
        email.setSubject(this.getSubject());        
        email.setMsg(PLAIN_CONTENT);
        email.addTo(this.getMailTo());
        
        email.buildMimeMessage();
        mimeMessage = email.getMimeMessage();
        
        this. result = this.getService().send(
            email.getMailSession(),
            mimeMessage
            );
    }
}
