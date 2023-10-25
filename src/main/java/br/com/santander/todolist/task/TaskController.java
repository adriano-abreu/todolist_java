package br.com.santander.todolist.task;



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

import br.com.santander.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  @Autowired
  private ITaskRepository taskRepository;
  
  @PostMapping("/")
  public ResponseEntity<?> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    taskModel.setIdUser((UUID) idUser);

    var currentDate = LocalDateTime.now();
    if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return new ResponseEntity<String>("Data de inicio ou término da tarefa não pode ser posterior a data atual", HttpStatus.BAD_REQUEST); 
    }

    if(taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return new ResponseEntity<String>("A data de início deve ser menor que a data de término", HttpStatus.BAD_REQUEST); 
    }

    var task = this.taskRepository.save(taskModel);
    return new ResponseEntity<TaskModel>(task, HttpStatus.CREATED);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    var tasks = this.taskRepository.findByIdUser((UUID) idUser);
    return tasks;
  }

  @PutMapping("/{id}")
  public  ResponseEntity<?> update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
  
    var task = this.taskRepository.findById(id).orElse(null);

    if (task == null) {
      return new ResponseEntity<String>("Tarefa não encontrada.", HttpStatus.BAD_REQUEST);
    }

    var idUser = request.getAttribute("idUser");

    if (!task.getIdUser().equals(idUser)) {
      return new ResponseEntity<String>("Tarefa não encontrada.", HttpStatus.BAD_REQUEST);
    }

    Utils.copyNonNullProperties(taskModel, task);
  
    var newTask = this.taskRepository.save(task);

    return new ResponseEntity<TaskModel>(newTask, HttpStatus.CREATED);
  }
}
