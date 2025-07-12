package win.pluvivanto.roombook.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import win.pluvivanto.roombook.domain.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

  List<Room> findByName(String name);
}
