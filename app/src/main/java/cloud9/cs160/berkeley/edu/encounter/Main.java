package cloud9.cs160.berkeley.edu.encounter;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.ListCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.NotificationTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManager;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCards;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCardsException;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteResourceStore;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteToqNotification;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.resource.CardImage;


public class Main extends Activity {

    private final static String PREFS_FILE= "prefs_file";
    private final static String DECK_OF_CARDS_KEY= "deck_of_cards_key";
    private final static String DECK_OF_CARDS_VERSION_KEY= "deck_of_cards_version_key";

    private DeckOfCardsManager mDeckOfCardsManager;
    private RemoteDeckOfCards mRemoteDeckOfCards;
    private RemoteResourceStore mRemoteResourceStore;
    private DeckOfCardsEventListener mListener;
    private CardImage[] mCardImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDeckOfCardsManager = DeckOfCardsManager.getInstance(getApplicationContext());
        // Create the resource store for icons and images
        mRemoteResourceStore= new RemoteResourceStore();
        ListCard listCard= new ListCard();
        mRemoteDeckOfCards = new RemoteDeckOfCards(this, listCard);
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

    public void sendNotification(View v) {
        String friend = "Jeffrey Zhang";

        String[] message = new String[1];
        message[0] = "NEARBY";
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
}
