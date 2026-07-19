package com.example.livebus.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Multi-tenant transit operator profile supporting both Local Government Corporations (e.g., UTC)
 * and Private Bus Company Owners (e.g., Stage Carriage / Charter operators).
 */
@Entity(tableName = "transit_operator")
data class TransitOperator(
    @PrimaryKey val operatorId: String,          // e.g., "GOVT_UTC", "PVT_SURESH_TRAVELS"
    val name: String,                            // e.g., "Uttarakhand Transport Corporation", "Suresh Coaches"
    val operatorType: String,                    // e.g., "PUBLIC_GOVT", "PRIVATE_STAGE_CARRIAGE", "PRIVATE_CHARTER"
    val contactPhone: String = "+91-135-2651111",
    val isVerifiedPartner: Boolean = true
)

/**
 * Core offline route entity storing compressed polyline geometry and corridor metadata.
 * Designed to fit within < 15MB total bundle size using encoded polylines instead of raw GeoJSON.
 */
@Entity(tableName = "govt_route")
data class GovtRoute(
    @PrimaryKey val routeId: Int,                // e.g., 101
    val operatorId: String,                      // FK to transit_operator
    val routeCode: String,                       // e.g., "UK-DDO-01", "PVT-HLD-04"
    val originHi: String,                        // e.g., "देहरादून (ISBT Dehradun)"
    val destinationHi: String,                   // e.g., "हल्द्वानी (Haldwani Bus Station)"
    val corridorType: String,                    // e.g., "MOUNTAIN_EXPRESS", "HILL_LOCAL"
    val encodedPolyline: String,                 // Compressed geometry (_p~iF~ps|U_ulLnnqC...)
    val totalDistanceMeters: Double = 275000.0,  // e.g., 275 km total corridor distance
    val versionTag: Int = 1                      // Used for differential delta sync
)

/**
 * Route stage milestone representing a physical bus stop / stage along the corridor.
 * Essential for rendering the 1D WIMT Linear Schematic UI and stage-wise fare verification.
 */
@Entity(
    tableName = "route_stage",
    primaryKeys = ["routeId", "stageId"],
    indices = [Index("sequenceNumber")]
)
data class RouteStageSegment(
    val routeId: Int,
    val stageId: Int,
    val sequenceNumber: Int,                     // 1, 2, 3... order along the route
    val nameEn: String,                          // e.g., "Bhimtal Lake Checkpoint"
    val nameHi: String,                          // e.g., "भीमताल झील बस स्टॉप"
    val accumulatedDistanceMeters: Double,       // Distance from route origin in meters (essential for 1D progress UI)
    val uphillAvgSpeedKmh: Int = 22,             // Hilly terrain speed profile (uphill)
    val downhillAvgSpeedKmh: Int = 38,           // Hilly terrain speed profile (downhill)
    val isUphill: Boolean = true,
    val ordinaryFareInr: Int = 35,               // State Ordinary fare up to this stage
    val expressFareInr: Int = 50                 // Express / Private Coach fare up to this stage
)

/**
 * Cellular network fingerprint mapping a specific cell tower (Cell ID / LAC / TAC)
 * directly to a 1D route milestone along the mountain polyline.
 * Enables zero-data, zero-GPS location tracking inside tunnels or dead zones.
 */
@Entity(
    tableName = "cell_fingerprint",
    indices = [Index(value = ["lac", "cid"], unique = false), Index("routeId")]
)
data class CellTowerFingerprint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routeId: Int,
    val cid: Int,                                // Cell Identity (CID / CI)
    val lac: Int,                                // Location Area Code (LAC / TAC)
    val mcc: Int = 404,                          // Mobile Country Code (India = 404/405)
    val mnc: Int = 45,                           // Mobile Network Code
    val projectedMilestoneMeters: Double,        // Exact mapped distance along the polyline in meters
    val signalRadiusMeters: Float = 350f         // Typical tower coverage radius along road corridor
)
