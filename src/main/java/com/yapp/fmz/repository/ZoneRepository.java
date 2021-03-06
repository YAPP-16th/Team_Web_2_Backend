package com.yapp.fmz.repository;

import com.yapp.fmz.domain.Zone;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long>, ZoneRepositoryCustom {

    @Query(value = "SELECT *, MAX(r.room_id) FROM zone AS z inner JOIN room AS r ON r.zipcode = z.zipcode GROUP BY z.zipcode", nativeQuery = true)
    public List<Zone> findZonesHasRoomV1();

    @Query(value = "SELECT distinct z from Zone z join fetch z.rooms Where size(z.rooms) >=1")
    public List<Zone> findZonesHasRoomV2();

    @Cacheable(value = "zoneHasRoomQuery")
    @Query(value = "SELECT distinct z from Zone z join fetch z.rooms Where size(z.rooms) >=1")
    public List<Zone> findZonesHasRoomV3();

    @Query(value = "SELECT distinct z from Zone z join fetch z.rooms Where z.id = 227 or z.id = 268 or z.id = 248")
    public List<Zone> findTestZonesHasRoom();

    @Query(value = "Select distinct z from Zone z join fetch z.rooms ")
    public List<Zone> findFetchAll();

    public List<Zone> findZonesByType(String type);
}
