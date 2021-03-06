package com.yapp.fmz.service;

import com.yapp.fmz.domain.Room;
import com.yapp.fmz.repository.RoomRepository;
import com.yapp.fmz.utils.KakaoApi;
import com.yapp.fmz.utils.PeterApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

@Service
public class RoomService {
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    PeterApi peterApi;
    @Autowired
    KakaoApi kakaoApi;


    public List<Room> findRoomsByMonthlyPayment(Long zone_id){
        return roomRepository.findRoomsByZoneOrderByMonthlyPayment(zone_id);
    }
    public List<Room> findRoomsByRegisterdId(Long zone_id){
        return roomRepository.findRoomsByZoneOrderByRegisterId(zone_id);
    }

    @Scheduled(cron="0 0 4 * * ?")
    @CacheEvict("zoneHasRoomQuery")
    public void removeTrashRooms(){
        int totalCount = 0;
        int trashCount = 0;
        int roomCount = 0;

        long l = System.currentTimeMillis();

        List<Room> all = roomRepository.findAll();
        totalCount = all.size();

        List<Room> removeList = new ArrayList<>();
        List<CompletableFuture<Room>> futureRemoveList = new ArrayList<>();
        for(int i=0; i<all.size(); i++){
            futureRemoveList.add(peterApi.removeTrashRooms(all.get(i), i));
        }

        for (CompletableFuture<Room> removeRoom : futureRemoveList) {
            Room join = removeRoom.join();
            if (join != null) {
                removeList.add(join);
            }
        }

        trashCount = removeList.size();
        roomCount = totalCount - trashCount;

        long time = System.currentTimeMillis() - l;
        System.out.println("전체 소요시간 : " + (time / 1000) + "초");

        System.out.println("전체 방 개수 : " + totalCount);
        System.out.println("정상 방 개수 : " + roomCount);
        System.out.println("쓰레기 방 개수 : " + trashCount);

        List<Long> collect = removeList.stream().map(Room::getId).collect(toList());
        roomRepository.deleteAllByIdInQuery(collect);

        SimpleDateFormat format2 = new SimpleDateFormat( "yyyy년 MM월dd일 HH시mm분ss초");
        Date now = new Date();
        String time2 = format2.format(now);
        kakaoApi.sendKakaoMessage("충성! 진호님" + time2 +  " 쓰레기 매물 삭제를 완료했습니다! 내일 4시에 다시 삭제하도록 하겠습니다! 충성!");
    }

}
