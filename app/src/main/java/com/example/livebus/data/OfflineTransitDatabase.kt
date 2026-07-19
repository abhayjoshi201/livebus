package com.example.livebus.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransitOperator::class,
        GovtRoute::class,
        RouteStageSegment::class,
        CellTowerFingerprint::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OfflineTransitDatabase : RoomDatabase() {

    abstract fun offlineTransitDao(): OfflineTransitDao

    companion object {
        @Volatile
        private var INSTANCE: OfflineTransitDatabase? = null

        fun getDatabase(context: Context): OfflineTransitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineTransitDatabase::class.java,
                    "offline_transit.db"
                )
                    .addCallback(OfflineTransitCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class OfflineTransitCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.offlineTransitDao())
                }
            }
        }

        suspend fun populateInitialData(dao: OfflineTransitDao) {
            // 1. Operators
            val operators = listOf(
                TransitOperator("GOVT_UTC", "Uttarakhand Transport Corp (UTC)", "PUBLIC_GOVT", "+91-135-2651111"),
                TransitOperator("PVT_SURESH", "Suresh Coaches & Shuttles", "PRIVATE_STAGE_CARRIAGE", "+91-9876543210")
            )

            // 2. Routes
            val routes = listOf(
                GovtRoute(
                    routeId = 101,
                    operatorId = "GOVT_UTC",
                    routeCode = "UK-DDO-01",
                    originHi = "देहरादून (ISBT Dehradun)",
                    destinationHi = "हल्द्वानी (Haldwani Bus Station)",
                    corridorType = "MOUNTAIN_EXPRESS",
                    encodedPolyline = "_p~iF~ps|U_ulLnnqC_seK...",
                    totalDistanceMeters = 275000.0
                ),
                GovtRoute(
                    routeId = 202,
                    operatorId = "PVT_SURESH",
                    routeCode = "PVT-BHT-04",
                    originHi = "भीमताल झील (Bhimtal Lake)",
                    destinationHi = "नैनीताल मल्लीताल (Nainital)",
                    corridorType = "HILL_LOCAL",
                    encodedPolyline = "_seK~ps|U_ulL...",
                    totalDistanceMeters = 22000.0
                )
            )

            // 3. Stages for Route 101 (Dehradun -> Haldwani)
            val stagesRoute101 = listOf(
                RouteStageSegment(101, 1, 1, "ISBT Dehradun Terminal", "ISBT देहरादून बस अड्डा", 0.0, 25, 30, false, 0, 0),
                RouteStageSegment(101, 2, 2, "Haridwar Bypass Circle", "हरिद्वार बाईपास चौक", 52000.0, 45, 55, false, 80, 110),
                RouteStageSegment(101, 3, 3, "Najibabad Checkpost", "नजीबाबाद चेकपोस्ट", 125000.0, 50, 55, false, 190, 240),
                RouteStageSegment(101, 4, 4, "Rudrapur City Junction", "रुद्रपुर सिडकुल मोड़", 235000.0, 45, 50, false, 320, 410),
                RouteStageSegment(101, 5, 5, "Haldwani Bus Depot", "हल्द्वानी रोडवेज डिपो", 275000.0, 35, 40, true, 380, 490)
            )

            // 4. Stages for Route 202 (Bhimtal -> Nainital - Private Stage Carriage)
            val stagesRoute202 = listOf(
                RouteStageSegment(202, 1, 1, "Bhimtal Lake Stand", "भीमताल डांठ (Lake Checkpoint)", 0.0, 22, 38, true, 0, 0),
                RouteStageSegment(202, 2, 2, "Bhowali Market Junction", "भवाली मुख्य बाज़ार चौराहा", 11000.0, 18, 32, true, 25, 35),
                RouteStageSegment(202, 3, 3, "Nainital Tallital Bus Stand", "नैनीताल तल्लीताल बस स्टैंड", 22000.0, 20, 30, true, 45, 65)
            )

            // 5. Sample Cell Tower Fingerprints (Pillar 2)
            val cellFingerprints = listOf(
                CellTowerFingerprint(routeId = 101, cid = 45102, lac = 1204, projectedMilestoneMeters = 51500.0),
                CellTowerFingerprint(routeId = 101, cid = 88301, lac = 1204, projectedMilestoneMeters = 124800.0),
                CellTowerFingerprint(routeId = 202, cid = 33109, lac = 5510, projectedMilestoneMeters = 10800.0)
            )

            dao.applyDeltaSync(operators, routes, stagesRoute101 + stagesRoute202, cellFingerprints)
        }
    }
}
