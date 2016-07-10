package ninja.mpnguyen.sanjosevehicleabatement;

import android.app.IntentService;
import android.content.Intent;

public class ReporterService extends IntentService {

    public ReporterService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        throw new UnsupportedOperationException("This method has not been implemented yet");
    }
}
