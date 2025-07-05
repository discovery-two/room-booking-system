package win.pluvivanto.roombook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import win.pluvivanto.roombook.domain.Room;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByName(String name);
}
