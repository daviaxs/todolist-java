package br.com.daviaxs.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.daviaxs.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    var currentDate = LocalDateTime.now();
    var taskStartAt = taskModel.getStartAt();
    var taskEndAt = taskModel.getEndAt();

    taskModel.setIdUser((UUID) idUser);

    if (currentDate.isAfter(taskStartAt) || currentDate.isAfter(taskEndAt)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("A data de início / data de término devem ser maior do que a data atual.");
    }

    if (taskStartAt.isAfter(taskEndAt)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("A data de início deve ser maior do que a data de término.");
    }

    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    var tasks = this.taskRepository.findByIdUser((UUID) idUser);

    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
    var task = this.taskRepository.findById(id).orElse(null);
    var idUser = request.getAttribute("idUser");

    if (!task.getIdUser().equals(idUser)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Usuário não tem permissão para alterar essa tarefa.");
    }

    Utils.copyNonNullProperties(taskModel, task);

    var taskUpdated = this.taskRepository.save(task);

    return ResponseEntity.ok().body(taskUpdated);
  }
}
