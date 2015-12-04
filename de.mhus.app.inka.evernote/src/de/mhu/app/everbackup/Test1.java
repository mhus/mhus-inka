package de.mhu.app.everbackup;
import java.util.*;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;

import com.evernote.edam.type.*;
import com.evernote.edam.userstore.*;
import com.evernote.edam.notestore.*;

public class Test1 {
    public static void main(String args[]) throws Exception {

        String username = "familiehummel";
        String password = "ichbin28";
        String consumerKey = "?";
        String consumerSecret = "1234abc";
        String userStoreUrl = "https://www.evernote.com/edam/user";
        String noteStoreUrlBase = "https://www.evernote.com/edam/note/";

        THttpClient userStoreTrans = new THttpClient(userStoreUrl);
        TBinaryProtocol userStoreProt = new TBinaryProtocol(userStoreTrans);
        UserStore.Client userStore =
            new UserStore.Client(userStoreProt, userStoreProt);

        boolean versionOk =
            userStore.checkVersion("",
               com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
                com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);

        if (!versionOk) {
            System.err.println("Incomatible EDAM client protocol version");
            return;

        }

        AuthenticationResult authResult =
            userStore.authenticate(username, password, consumerKey, consumerSecret);

//        AuthConsumer consumer = new DefaultOAuthConsumer( "myConsumerKey", "myConsumerSecret" );
//        consumer.setMessageSigner( new PlainTextMessageSigner() );
//        OAuthProvider provider = new DefaultOAuthProvider( "https://sandbox.evernote.com/oauth", "https://sandbox.evernote.com/oauth", "https://sandbox.evernote.com/OAuth.action" );
//        String authUrl = provider.retrieveRequest( consumer, OAuth.OUT_OF_BAND );


        User user = authResult.getUser();
        String authToken = authResult.getAuthenticationToken();
        System.out.println("Notes for " + user.getUsername() + ":");

        String noteStoreUrl = noteStoreUrlBase + user.getShardId();
        THttpClient noteStoreTrans = new THttpClient(noteStoreUrl);
        TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
        NoteStore.Client noteStore =
            new NoteStore.Client(noteStoreProt, noteStoreProt);

        List<Notebook> notebooks =
            (List<Notebook>) noteStore.listNotebooks(authToken);

        for (Notebook notebook : notebooks) {
            System.out.println("Notebook: " + notebook.getName());

            NoteFilter filter = new NoteFilter();
            filter.setNotebookGuid(notebook.getGuid());
            NoteList noteList = noteStore.findNotes(authToken, filter, 0, 100);
            List<Note> notes = (List<Note>) noteList.getNotes();

            for (Note note : notes) {
               System.out.println(" * " + note.getTitle());
            }     
        }
     }
}
