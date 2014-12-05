package cloud9.cs160.berkeley.edu.encounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Mari on 9/14/14.
 */
public class ToqBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch the  activity to complete the install of the deck of cards applet
        Intent launchIntent= new Intent(context, Main.class);
        //launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(launchIntent);
    }
}
