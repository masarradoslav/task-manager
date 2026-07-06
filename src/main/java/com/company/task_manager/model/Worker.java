package com.company.task_manager.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import org.hibernate.annotations.DiscriminatorOptions;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

// Manager a RegularEmployee sú konkrétne podtypy (single-table dedičnosť, stĺpec worker_type
// rozlišuje riadky) - samotná trieda Worker sa nikdy neukladá priamo, slúži len ako spoločný
// základ a ako dočasný "len id" placeholder pri prepájaní na existujúceho workera (pozri
// TaskController.toEntity), kým službová vrstva nenahradí referenciu skutočnou entitou.
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "worker_type")
// force = true zabráni Hibernate vygenerovať CHECK (worker_type IN (...)) constraint na
// diskriminátorovom stĺpci. Ten IN-check má v H2 in-memory DB chybu: constraint si zapamätá
// session, ktorá ho prvýkrát vyhodnotila (connection zo schema-generation), a keď HikariCP tú
// connection neskôr uzavrie, každé ďalšie INSERT/UPDATE na worker padne s
// "Check constraint invalid / The database has been closed [90098]". Diskriminátor píše len
// Hibernate (nikdy nie user), takže o žiadnu ochranu neprichádzame; force=true namiesto toho
// pridáva podmienku na worker_type priamo do SELECT-ov.
@DiscriminatorOptions(force = true)
public class Worker {

    @Id
    @GeneratedValue
    private int id;

    private String name;

    @Column(unique = true)
    private String username;

    // BCrypt hash, nikdy sa neserializuje do WorkerResponse
    private String password;

    @Enumerated(EnumType.STRING)
    private Department department;

    // mappedBy = "worker" znamená, že vlastníkom vzťahu je pole "worker" v triede Task
    // Entita sa už priamo neserializuje (controllery vracajú WorkerResponse DTO), takže
    // netreba @JsonIgnore na predídenie nekonečnej rekurzii Worker -> tasks -> Task -> worker ...
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL)
    private Collection<Task> tasks;

    // Bezparametrický konštruktor - JPA ho vyžaduje
    public Worker() {
        this.tasks = new HashSet<>();
    }

    public Worker(int id) {
        this.id = id;
    }

    public Worker(String name) {
        this.name = name;
        this.tasks = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Collection<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Collection<Task> tasks) {
        this.tasks = tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
        task.setWorker(this); // dôležité - udržuje konzistenciu vzťahu na oboch stranách
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Worker worker = (Worker) o;
        return id == worker.id && Objects.equals(name, worker.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
