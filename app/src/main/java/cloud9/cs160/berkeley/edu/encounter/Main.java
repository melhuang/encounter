package cloud9.cs160.berkeley.edu.encounter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    private DeckOfCardsManagerListener deckOfCardsManagerListener;
    private DeckOfCardsEventListener deckOfCardsEventListener;
    private DeckOfCardsManager mDeckOfCardsManager;
    private RemoteDeckOfCards mRemoteDeckOfCards;
    private RemoteResourceStore mRemoteResourceStore;
    private DeckOfCardsEventListener mListener;
    private CardImage[] mCardImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//        setContentView(R.layout.fragment_login);

        deckOfCardsManagerListener= new DeckOfCardsManagerListenerImpl();
        deckOfCardsEventListener= new DeckOfCardsEventListenerImpl();


        mDeckOfCardsManager = DeckOfCardsManager.getInstance(getApplicationContext());
        // Create the resource store for icons and images
        mRemoteResourceStore= new RemoteResourceStore();
//        ListCard listCard= new ListCard();
//        mRemoteDeckOfCards = new RemoteDeckOfCards(this, listCard);
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
        updateDeckOfCardsFromUI();
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

    private RemoteDeckOfCards createDeckOfCards(){
        ListCard listCard= new ListCard();
        Bitmap image;
        CardImage cardImage;

        SimpleTextCard dnd= new SimpleTextCard("dnd");
        dnd.setHeaderText("Do Not Disturb");
        dnd.setReceivingEvents(true);
        listCard.add(dnd);

        SimpleTextCard meetup= new SimpleTextCard("Meetup");
        meetup.setHeaderText("Meetup");
        meetup.setReceivingEvents(true);
        listCard.add(meetup);

        SimpleTextCard nudge= new SimpleTextCard("Nudge");
        nudge.setHeaderText("Nudge");
        nudge.setReceivingEvents(true);
        listCard.add(nudge);

        SimpleTextCard come= new SimpleTextCard("Come");
        come.setHeaderText("Come to me");
        come.setReceivingEvents(false);
        listCard.add(come);

        SimpleTextCard directions= new SimpleTextCard("Directions");
        directions.setHeaderText("Directions");
        directions.setTitleText("30 feet");
        image = BitmapFactory.decodeResource(getResources(),
                R.drawable.arrow);
        cardImage = new CardImage("joan", image);
        directions.setCardImage(mRemoteResourceStore, cardImage);
        directions.setReceivingEvents(true);
        listCard.add(directions);


        return new RemoteDeckOfCards(this, listCard);
    }


    public void meetupRequest(View v) {
        String friend = "Anna Lee";

        String[] message = new String[1];
        message[0] = "wants to meet up.";
        // Create a NotificationTextCard
        NotificationTextCard notificationCard = new NotificationTextCard(System.currentTimeMillis(),
                friend, message);

        // Draw divider between lines of text
        notificationCard.setShowDivider(false);
        // Vibrate to alert user when showing the notification
        notificationCard.setVibeAlert(true);
        RemoteToqNotification notification = new RemoteToqNotification(this, notificationCard);

        try {
            // Send the notification
            mDeckOfCardsManager.sendNotification(notification);
            Toast.makeText(this, "Sent Notification", Toast.LENGTH_SHORT).show();
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send Notification", Toast.LENGTH_SHORT).show();
        }
    }

    public void nudgeRequest(View v) {
        String friend = "Anna Lee";

        String[] message = new String[1];
        message[0] = "nudged you.";
        // Create a NotificationTextCard
        NotificationTextCard notificationCard = new NotificationTextCard(System.currentTimeMillis(),
                friend, message);

        // Draw divider between lines of text
        notificationCard.setShowDivider(false);
        // Vibrate to alert user when showing the notification
        notificationCard.setVibeAlert(true);
        RemoteToqNotification notification = new RemoteToqNotification(this, notificationCard);

        try {
            // Send the notification
            mDeckOfCardsManager.sendNotification(notification);
            Toast.makeText(this, "Sent Notification", Toast.LENGTH_SHORT).show();
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send Notification", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendNotification(View v) {
        String friend = "Anna Lee";

        String[] message = new String[1];
        message[0] = "is nearby.";
        // Create a NotificationTextCard
        NotificationTextCard notificationCard = new NotificationTextCard(System.currentTimeMillis(),
                friend, message);

        // Draw divider between lines of text
        notificationCard.setShowDivider(false);
        // Vibrate to alert user when showing the notification
        notificationCard.setVibeAlert(true);
        RemoteToqNotification notification = new RemoteToqNotification(this, notificationCard);

        try {
            // Send the notification
            mDeckOfCardsManager.sendNotification(notification);
            Toast.makeText(this, "Sent Notification", Toast.LENGTH_SHORT).show();
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send Notification", Toast.LENGTH_SHORT).show();
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
//            Log.e("onCardOpen", cardId);

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
//            Log.e("onCardVisible", cardId);

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
//            Log.e("onMenuOptionSelected", cardId + " " + menuOption);

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
}
