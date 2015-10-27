package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by DivyaM on 10/23/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_ID = 8;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            private String[] fragmentdate = new String[1];

            @Override
            public void onCreate() {
                //Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission


                final long identityToken = Binder.clearCallingIdentity();
                Date fragmentDate = new Date(System.currentTimeMillis() + (0 * 86400000));
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                fragmentdate[0] = mformat.format(fragmentDate);

                // Get today's data from the ContentProvider
                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        null,
                        null,
                        fragmentdate,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);
                // Extract the data from the Cursor
                String homeName = data.getString(COL_HOME);
                String awayName = data.getString(COL_AWAY);
                String date = data.getString(COL_MATCHTIME);
                String score = Utilities.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS));
                int homeResourceId = Utilities.getTeamCrestByTeamName(
                        data.getString(COL_HOME));
                int awayResourceId = Utilities.getTeamCrestByTeamName(
                        data.getString(COL_AWAY));

                 //Add the data to the RemoteViews
                views.setImageViewResource(R.id.widget_home_icon, homeResourceId);
                views.setImageViewResource(R.id.widget_away_icon, awayResourceId);
                // Content Descriptions for RemoteViews were only added in ICS MR1
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, homeName, awayName);
                }
                views.setTextViewText(R.id.widget_home_name, homeName);
                views.setTextViewText(R.id.widget_score_textview, score);
                views.setTextViewText(R.id.widget_away_name, awayName);
                views.setTextViewText(R.id.widget_date, date);

                final Intent fillInIntent = new Intent();

                Uri ScoresUri = DatabaseContract.scores_table.buildScoreWithDate();
                fillInIntent.setData(ScoresUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;

            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String homeDescription, String awayDescription) {
                views.setContentDescription(R.id.widget_home_icon, homeDescription);
                views.setContentDescription(R.id.widget_away_icon, awayDescription);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(COL_ID); //To do
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}