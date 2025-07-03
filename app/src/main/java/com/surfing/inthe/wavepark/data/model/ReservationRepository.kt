package com.surfing.inthe.wavepark.data.model

import com.surfing.inthe.wavepark.data.database.dao.ReservationDao
import com.surfing.inthe.wavepark.data.database.entity.ReservationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor(
    private val reservationDao: ReservationDao
) {
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    // RoomDB에서 예약 데이터를 가져와서 도메인 모델로 변환
    fun getUpcomingReservations(): Flow<List<Reservation>> {
        return reservationDao.getUpcomingReservations(Date()).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getAllReservations(): Flow<List<Reservation>> {
        return reservationDao.getAllReservations().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun insertReservation(reservation: Reservation): Long {
        return reservationDao.insertReservation(reservation.toEntity())
    }

    suspend fun insertReservations(reservations: List<Reservation>) {
        reservationDao.insertReservations(reservations.map { it.toEntity() })
    }

    suspend fun updateReservation(reservation: Reservation) {
        reservationDao.updateReservation(reservation.toEntity())
    }

    suspend fun deleteReservation(reservation: Reservation) {
        reservationDao.deleteReservation(reservation.toEntity())
    }

    suspend fun deleteReservationByNumber(reservationNumber: String) {
        reservationDao.deleteReservationByNumber(reservationNumber)
    }

    suspend fun clearAllReservations() {
        reservationDao.deleteAllReservations()
    }

    suspend fun getUpcomingReservationCount(): Int {
        return reservationDao.getUpcomingReservationCount(Date())
    }

    fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    fun clear() {
        _loading.value = true
    }

    // WebViewFragment에서 사용하는 메서드들
    suspend fun addReservations(reservations: List<Reservation>) {
        reservationDao.insertReservations(reservations.map { it.toEntity() })
    }

    // Entity를 도메인 모델로 변환하는 확장 함수
    private fun ReservationEntity.toDomainModel(): Reservation {
        return Reservation(
            reservationNumber = reservationNumber,
            sessionDate = sessionDate,
            sessionTime = sessionTime,
            sessionType = sessionType,
            remainingSeats = remainingSeats,
            totalSeats = totalSeats,
            price = price,
            status = status
        )
    }

    // 도메인 모델을 Entity로 변환하는 확장 함수
    private fun Reservation.toEntity(): ReservationEntity {
        return ReservationEntity(
            reservationNumber = reservationNumber,
            sessionDate = sessionDate,
            sessionTime = sessionTime,
            sessionType = sessionType,
            remainingSeats = remainingSeats,
            totalSeats = totalSeats,
            price = price,
            status = status
        )
    }
} 