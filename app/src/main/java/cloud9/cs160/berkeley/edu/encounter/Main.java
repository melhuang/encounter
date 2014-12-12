package cloud9.cs160.berkeley.edu.encounter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.ListCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.NotificationTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.SimpleTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManager;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCards;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCardsException;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteResourceStore;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteToqNotification;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.resource.CardImage;


public class Main extends Activity {

    private final static String PREFS_FILE= "prefs_file";
    private final static String DECK_OF_CARDS_KEY= "deck_of_cards_key";
    private final static String DECK_OF_CARDS_VERSION_KEY= "deck_of_cards_version_key";
    public static final String NUDGE = "Nudge";
    public static final String LETS_MEET = "Let's Meet";
    public static final String COMING_TO_YOU = "Coming to you";
    public static final String COME_TO_ME = "Come to me";

    private DeckOfCardsManagerListener deckOfCardsManagerListener;
    private DeckOfCardsEventListener deckOfCardsEventListener;
    private DeckOfCardsManager mDeckOfCardsManager;
    private RemoteDeckOfCards mRemoteDeckOfCards;
    private RemoteResourceStore mRemoteResourceStore;
    private DeckOfCardsEventListener mListener;
    private CardImage[] mCardImages;

    private String friend = "Melissa Huang";
    private EditText friendEdit;
    private String appName = "Encounter";
    private boolean dndStatus = false;
    private boolean invited = false;
    private boolean inviting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        friendEdit = (EditText) findViewById(R.id.editText);
        friendEdit.setText(friend);
        friendEdit.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                        friend = charSequence.toString();
                        ListCard listCard = mRemoteDeckOfCards.getListCard();
                        SimpleTextCard card = (SimpleTextCard) listCard.get("encounter");
                        card.setTitleText(friend);
                        updateDeck(null);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                }
        );

        deckOfCardsManagerListener= new DeckOfCardsManagerListenerImpl();
        deckOfCardsEventListener= new DeckOfCardsEventListenerImpl();


        mDeckOfCardsManager = DeckOfCardsManager.getInstance(getApplicationContext());
        mDeckOfCardsManager.addDeckOfCardsEventListener(deckOfCardsEventListener);
        // Create the resource store for icons and images
        mRemoteResourceStore= new RemoteResourceStore();
//        ListCard listCard= new ListCard();
//        mRemoteDeckOfCards = new RemoteDeckOfCards(this, listCard);

        updateDeckOfCardsFromUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mDeckOfCardsManager.isConnected()){
            try{
                mDeckOfCardsManager.connect();
            }
            catch (RemoteDeckOfCardsException e){
                e.printStackTrace();
            }
        }
    }

    public void login(View v) {
        setContentView(R.layout.activity_main);
    }

    /**
     * Installs applet to Toq watch if app is not yet installed
     */
    public void install(View v) {
        boolean isInstalled = true;

        try {
            isInstalled = mDeckOfCardsManager.isInstalled();
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: Can't determine if app is installed", Toast.LENGTH_SHORT).show();
        }

        if (!isInstalled) {
            try {
                mDeckOfCardsManager.installDeckOfCards(mRemoteDeckOfCards, mRemoteResourceStore);
            } catch (RemoteDeckOfCardsException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error: Cannot install application", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "App is already installed!", Toast.LENGTH_SHORT).show();
        }
    }

    public void uninstall(View v) {
        boolean isInstalled = true;

        try {
            isInstalled = mDeckOfCardsManager.isInstalled();
        }
        catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: Can't determine if app is installed", Toast.LENGTH_SHORT).show();
        }

        if (isInstalled) {
            try{
                mDeckOfCardsManager.uninstallDeckOfCards();
                Toast.makeText(this, "Uninstalled", Toast.LENGTH_SHORT).show();
            }
            catch (RemoteDeckOfCardsException e){
                Toast.makeText(this, "Error uninstalling deck of cards", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Already uninstalled", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDeckOfCardsFromUI() {
        if (mRemoteDeckOfCards == null) {
            mRemoteDeckOfCards = createDeckOfCards();
        }
    }

    public void updateDeck(View v) {
        if (v != null) {
            invited = false;
            inviting = false;
            mRemoteDeckOfCards = createDeckOfCards();
        }
        try {
            mDeckOfCardsManager.updateDeckOfCards(mRemoteDeckOfCards, mRemoteResourceStore);
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    private void setActive(SimpleTextCard dnd) {
        dnd.setTitleText("Active");
        String[] menuOptions = {"Do Not Disturb"};
        dnd.setMenuOptions(menuOptions);
        dndStatus = false;
    }

    private void setDND(SimpleTextCard dnd) {
        dnd.setTitleText("Do Not Disturb");
        String[] menuOptions = {"Active"};
        dnd.setMenuOptions(menuOptions);
        dndStatus = true;
    }

    private void toggleStatus(SimpleTextCard dnd) {
        if (dndStatus) {
            setActive(dnd);
        } else setDND(dnd);
    }

    private RemoteDeckOfCards createDeckOfCards(){
        ListCard listCard= new ListCard();
        Bitmap image;
        CardImage cardImage;

        SimpleTextCard dnd= new SimpleTextCard("dnd");
        dnd.setHeaderText("Status");
        setActive(dnd);
        dnd.setReceivingEvents(true);
        listCard.add(dnd);

        SimpleTextCard encounterCard = new SimpleTextCard("encounter");
        encounterCard.setHeaderText("New Encounter");
        encounterCard.setTitleText(friend);
        encounterCard.setReceivingEvents(true);
        String[] options = {NUDGE, LETS_MEET};
        encounterCard.setMenuOptions(options);
        listCard.add(encounterCard);

//        SimpleTextCard meetup= new SimpleTextCard("Meetup");
//        meetup.setHeaderText("Meetup");
//        meetup.setReceivingEvents(true);
//        encounterCard.add(meetup);
//
//        SimpleTextCard nudge= new SimpleTextCard("Nudge");
//        nudge.setHeaderText("Nudge");
//        nudge.setReceivingEvents(true);
//        encounterCard.add(nudge);

        return new RemoteDeckOfCards(this, listCard);
    }

    private boolean meetup() {
        if (invited && inviting) {
            ListCard listCard = mRemoteDeckOfCards.getListCard();
            SimpleTextCard encounterCard = (SimpleTextCard) listCard.get("encounter");
            encounterCard.setHeaderText("Meeting with");
            encounterCard.setTitleText(friend);
            String[] options = {COMING_TO_YOU, COME_TO_ME};
            encounterCard.setMenuOptions(options);

            String[] messages = {"30 feet"};
            encounterCard.setMessageText(messages);
            Bitmap image = BitmapFactory.decodeResource(getResources(),
                    R.drawable.arrow);
            image = Bitmap.createScaledBitmap(image, 250, 288, false);
            CardImage cardImage = new CardImage("joan", image);
            encounterCard.setCardImage(mRemoteResourceStore, cardImage);

            updateDeck(null);

            sendNotification("Invite accepted.");
            return true;
        }
        return false;
    }

    public void comingToYou(View v) {
        sendNotification("says: coming to you.");
    }

    public void comeToMe(View v) {
        sendNotification("says: come to me.");
    }

    public void meetupRequest(View v) {
        sendNotification("wants to meet up.");
        invited = true;
        meetup();
    }

    public void nudgeRequest(View v) {
        sendNotification("nudged you.");
    }

    public void sendNotification(View v) {
        sendNotification("is nearby.");
    }

    public void sendNotification(String s) {
        if(dndStatus) {
            return;
        }
        String[] message = new String[1];
        message[0] = s;
        // Create a NotificationTextCard
        NotificationTextCard notificationCard = new NotificationTextCard(System.currentTimeMillis(),
                friend, message);

        // Draw divider between lines of text
        notificationCard.setShowDivider(false);
        // Vibrate to alert user when showing the notification
        notificationCard.setVibeAlert(true);
        notificationCard.setReceivingEvents(true);
        if (!invited || !inviting) {
            String[] options = {NUDGE, LETS_MEET};
            notificationCard.setMenuOptions(options);
        }
        RemoteToqNotification notification = new RemoteToqNotification(this, notificationCard);

        try {
            // Send the notification
            mDeckOfCardsManager.sendNotification(notification);
            Toast.makeText(this, "Sent Notification", Toast.LENGTH_SHORT).show();
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send Notification", Toast.LENGTH_SHORT).show();
        }

        if (!invited || !inviting) {
            ListCard listCard = mRemoteDeckOfCards.getListCard();
            SimpleTextCard card = (SimpleTextCard) listCard.get("encounter");
            String[] messages = {s};
            card.setMessageText(messages);
            updateDeck(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Handle service connection lifecycle and installation events
    private class DeckOfCardsManagerListenerImpl implements DeckOfCardsManagerListener{

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onConnected()
         */
        public void onConnected(){
            runOnUiThread(new Runnable(){
                public void run(){
//                    setStatus("connected");
//                    Log.e("DeckOfCardsManagerListenerImpl", "connected");
                    //refreshUI();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onDisconnected()
         */
        public void onDisconnected(){
            runOnUiThread(new Runnable(){
                public void run(){
//                    setStatus(getString(R.string.status_disconnected));
//                    disableUI();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onInstallationSuccessful()
         */
        public void onInstallationSuccessful(){
            runOnUiThread(new Runnable(){
                public void run(){
//                    setStatus(getString(R.string.status_installation_successful));
//                    updateUIInstalled();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onInstallationDenied()
         */
        public void onInstallationDenied(){
            runOnUiThread(new Runnable(){
                public void run(){
//                    setStatus(getString(R.string.status_installation_denied));
//                    updateUINotInstalled();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onUninstalled()
         */
        public void onUninstalled(){
            runOnUiThread(new Runnable(){
                public void run(){
//                    setStatus(getString(R.string.status_uninstalled));
//                    updateUINotInstalled();
                }
            });
        }

    }

    // Handle card events triggered by the user interacting with a card in the installed deck of cards
    private class DeckOfCardsEventListenerImpl implements DeckOfCardsEventListener{

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardOpen(java.lang.String)
         */
        public void onCardOpen(final String cardId){
            log("onCardOpen " + cardId);

            runOnUiThread(new Runnable(){
                public void run(){
//                    if (notified) {
//                        if (cardId.equals(randomPerson)) {
////                            Log.e("YAYYY", "YAYYYY");
//                            Intent intent = new Intent(CanvasActivity.this, CanvasActivity.class);
//                            startActivity(intent);
//                        }
//                    }
                    Toast.makeText(getApplicationContext(), cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardVisible(java.lang.String)
         */
        public void onCardVisible(final String cardId){
            log("onCardVisible " + cardId);

            runOnUiThread(new Runnable(){
                public void run(){
//                    Toast.makeText(this,  getString(R.string.event_card_visible) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardInvisible(java.lang.String)
         */
        public void onCardInvisible(final String cardId){
            log("onCardInvisible " + cardId);
            runOnUiThread(new Runnable(){
                public void run(){
//                    Toast.makeText(ToqApiDemo.this, getString(R.string.event_card_invisible) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardClosed(java.lang.String)
         */
        public void onCardClosed(final String cardId){
            log("onCardClosed " + cardId);
//            if(cardId.equals("encounter")) {
//                ListCard listCard = mRemoteDeckOfCards.getListCard();
//                SimpleTextCard card = (SimpleTextCard) listCard.get("encounter");
//                card.setMessageText(null);
//                updateDeck(null);
//            }
            runOnUiThread(new Runnable(){
                public void run(){
//                    Toast.makeText(ToqApiDemo.this, getString(R.string.event_card_closed) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onMenuOptionSelected(java.lang.String, java.lang.String)
         */
        public void onMenuOptionSelected(final String cardId, final String menuOption){
            log("onMenuOptionSelected " + cardId + " " + menuOption);

            if(cardId.equals("dnd")) {
                ListCard listCard = mRemoteDeckOfCards.getListCard();
                SimpleTextCard card = (SimpleTextCard) listCard.get("dnd");
                toggleStatus(card);
            } else if (cardId.equals("encounter")) {
                ListCard listCard = mRemoteDeckOfCards.getListCard();
                SimpleTextCard card = (SimpleTextCard) listCard.get("encounter");
                if (menuOption.equals(NUDGE)) {
                    String[] messages = new String[1];
                    messages[0] = "Nudged";
                    card.setMessageText(messages);
                } else if (menuOption.equals(LETS_MEET)) {
                    inviting = true;
                    if (meetup()) {
                        return;
                    }
                    String[] messages = {"Invite Sent"};
                    card.setMessageText(messages);
                } else if (menuOption.equals(COMING_TO_YOU)) {
                    String[] options = {"You said: " + COMING_TO_YOU, COME_TO_ME};
                    card.setMenuOptions(options);
                } else if (menuOption.equals(COME_TO_ME)) {
                    String[] options = {COMING_TO_YOU, "You said: " + COME_TO_ME};
                    card.setMenuOptions(options);
                }
            } else if (cardId.equals(NotificationTextCard.ID)) {
                if (menuOption.equals(NUDGE)) {
                    sendNotification("Nudged");
                } else if (menuOption.equals(LETS_MEET)) {
                    inviting = true;
                    if (meetup()) {
                        return;
                    }
                    sendNotification("Invite Sent");
                }
            }

            updateDeck(null);

            runOnUiThread(new Runnable(){
                public void run(){
//                    Toast.makeText(ToqApiDemo.this, getString(R.string.event_menu_option_selected) + cardId + " [" + menuOption + "]", Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onMenuOptionSelected(java.lang.String, java.lang.String, java.lang.String)
         */
        public void onMenuOptionSelected(final String cardId, final String menuOption, final String quickReplyOption){
            runOnUiThread(new Runnable(){
                public void run(){
//                    Toast.makeText(ToqApiDemo.this, getString(R.string.event_menu_option_selected) + cardId + " [" + menuOption + ":" + quickReplyOption +
//                            "]", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void log(String s) {
        Log.v(appName, s);
    }
}
