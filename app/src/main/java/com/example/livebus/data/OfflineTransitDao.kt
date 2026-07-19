package com.example.livebus.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface OfflineTransitDao {

    // --- Operators ---
    @Query("SELECT * FROM transit_operator ORDER BY name ASC")
    fun getAllOperators(): Flow<List<TransitOperator>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperators(operators: List<TransitOperator>): List<Long>

    // --- Routes ---
    @Query("SELECT * FROM govt_route ORDER BY routeCode ASC")
    fun getAllRoutes(): Flow<List<GovtRoute>>

    @Query("SELECT * FROM govt_route WHERE routeId = :routeId")
    suspend fun getRouteById(routeId: Int): GovtRoute?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<GovtRoute>): List<Long>

    // --- Route Stages & 1D Schematic Data ---
    @Query("SELECT * FROM route_stage WHERE routeId = :routeId ORDER BY sequenceNumber ASC")
    fun getStagesForRoute(routeId: Int): Flow<List<RouteStageSegment>>

    @Query("SELECT * FROM route_stage WHERE routeId = :routeId ORDER BY sequenceNumber ASC")
    suspend fun getStagesForRouteSync(routeId: Int): List<RouteStageSegment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(stages: List<RouteStageSegment>): List<Long>

    // --- Offline Cell Tower Triangulation (Pillar 2) ---
    @Query("SELECT * FROM cell_fingerprint WHERE cid = :cid AND lac = :lac AND routeId = :routeId LIMIT 1")
    suspend fun getMilestoneOffline(cid: Int, lac: Int, routeId: Int): CellTowerFingerprint?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCellFingerprints(fingerprints: List<CellTowerFingerprint>): List<Long>
}

// --- Batch Delta Sync Transaction ---
suspend fun OfflineTransitDao.applyDeltaSync(
    operators: List<TransitOperator>,
    routes: List<GovtRoute>,
    stages: List<RouteStageSegment>,
    fingerprints: List<CellTowerFingerprint>
): Boolean {
    if (operators.isNotEmpty()) insertOperators(operators)
    if (routes.isNotEmpty()) insertRoutes(routes)
    if (stages.isNotEmpty()) insertStages(stages)
    if (fingerprints.isNotEmpty()) insertCellFingerprints(fingerprints)
    return true
}
