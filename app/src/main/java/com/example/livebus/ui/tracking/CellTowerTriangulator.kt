package com.example.livebus.ui.tracking

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.*
import com.example.livebus.data.CellTowerFingerprint
import com.example.livebus.data.OfflineTransitDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CellTowerTriangulator (Option B / Pillar 2):
 * Scans connected cellular base stations (Cell ID / LAC / TAC) and maps them directly
 * to 1D route milestones stored in the offline Room database.
 * 
 * Why this is crucial for Uttarakhand mountain routes (Dehradun / Bhimtal / Haldwani):
 * Inside valleys and metal bus cabins, GPS signals frequently time out or drain battery rapidly.
 * Because bus routes follow fixed roads, knowing which Cell Tower (cid, lac) the phone is
 * connected to instantly pinpoints the bus's 1D position along the route with 0 KB data consumed.
 */
object CellTowerTriangulator {

    data class ActiveCellIdentity(
        val cid: Int,
        val lac: Int,
        val mcc: Int,
        val mnc: Int,
        val networkType: String
    )

    /**
     * Scans the currently connected serving base station without requiring mobile data.
     * Requires ACCESS_FINE_LOCATION and READ_PHONE_STATE permissions.
     */
    @SuppressLint("MissingPermission")
    fun getServingCellIdentity(context: Context): ActiveCellIdentity? {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return null

        val cellInfoList = try {
            telephonyManager.allCellInfo
        } catch (e: SecurityException) {
            null
        } ?: return null

        // Find the actively registered (serving) cell tower
        val servingCell = cellInfoList.firstOrNull { it.isRegistered } ?: return null

        return when (servingCell) {
            is CellInfoLte -> {
                val id = servingCell.cellIdentity
                if (id.ci != Int.MAX_VALUE && id.ci != 0) {
                    val mcc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) id.mccString?.toIntOrNull() ?: 404 else id.mcc
                    val mnc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) id.mncString?.toIntOrNull() ?: 45 else id.mnc
                    ActiveCellIdentity(cid = id.ci, lac = id.tac, mcc = mcc, mnc = mnc, networkType = "LTE")
                } else null
            }
            is CellInfoGsm -> {
                val id = servingCell.cellIdentity
                if (id.cid != Int.MAX_VALUE && id.cid != 0) {
                    val mcc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) id.mccString?.toIntOrNull() ?: 404 else id.mcc
                    val mnc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) id.mncString?.toIntOrNull() ?: 45 else id.mnc
                    ActiveCellIdentity(cid = id.cid, lac = id.lac, mcc = mcc, mnc = mnc, networkType = "GSM")
                } else null
            }
            is CellInfoWcdma -> {
                val id = servingCell.cellIdentity
                if (id.cid != Int.MAX_VALUE && id.cid != 0) {
                    val mcc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) id.mccString?.toIntOrNull() ?: 404 else id.mcc
                    val mnc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) id.mncString?.toIntOrNull() ?: 45 else id.mnc
                    ActiveCellIdentity(cid = id.cid, lac = id.lac, mcc = mcc, mnc = mnc, networkType = "3G/WCDMA")
                } else null
            }
            else -> null
        }
    }

    /**
     * Resolves the current 1D milestone distance along the route offline by matching
     * the phone's connected cell tower against Room database records.
     * 
     * @return Projected milestone in meters, or null if cell tower is unknown/unmapped.
     */
    suspend fun scanOfflineCellFingerprint(
        context: Context,
        activeRouteId: Int,
        dao: OfflineTransitDao
    ): Double? = withContext(Dispatchers.IO) {
        val servingCell = getServingCellIdentity(context) ?: return@withContext null

        val fingerprint = dao.getMilestoneOffline(servingCell.cid, servingCell.lac, activeRouteId)
        fingerprint?.projectedMilestoneMeters
    }

    /**
     * Conductor Wardriving Helper:
     * Records the currently connected cell tower and pairs it with the current snapped polyline
     * milestone into Room DB. Used during initial calibration trips across mountain corridors.
     */
    suspend fun recordWardrivingFingerprint(
        context: Context,
        activeRouteId: Int,
        currentMilestoneMeters: Double,
        dao: OfflineTransitDao
    ): Boolean = withContext(Dispatchers.IO) {
        val servingCell = getServingCellIdentity(context) ?: return@withContext false

        val newFingerprint = CellTowerFingerprint(
            routeId = activeRouteId,
            cid = servingCell.cid,
            lac = servingCell.lac,
            mcc = servingCell.mcc,
            mnc = servingCell.mnc,
            projectedMilestoneMeters = currentMilestoneMeters,
            signalRadiusMeters = 350f
        )

        dao.insertCellFingerprints(listOf(newFingerprint))
        true
    }
}
