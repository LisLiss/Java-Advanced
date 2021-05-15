package ru.ifmo.rain.Nikolaeva.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentGroupQuery {

    private static final Comparator<Student> comparatorByName = Comparator
            .comparing(Student::getLastName, String::compareTo)
            .thenComparing(Student::getFirstName, String::compareTo)
            .thenComparing(Student::compareTo);

    private List<String> mapperList(List<Student> students, Function<Student, String> mapper) {
        return collectToList(students.stream().map(mapper));
    }

    private <T, C extends Collection<T>> C collectTo(Stream<T> stream, Supplier<C> supplier) {
        return stream.collect(Collectors.toCollection(supplier));
    }

    private <T> List<T> collectToList(Stream<T> stream) {
        return collectTo(stream, ArrayList::new);
    }


    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapperList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapperList(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return mapperList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapperList(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return collectTo(students.stream().map(Student::getFirstName), TreeSet::new);
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Student::compareTo).map(Student::getFirstName).orElse("");
    }

    private List<Student> sortStudents(Collection<Student> students, Comparator<Student> comparator) {
        return collectToList(students.stream().sorted(comparator));
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudents(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students, comparatorByName);
    }

    private List<Student> findStudents(Collection<Student> students, String equalString, Function<Student, String> mapper) {
        return sortStudentsByName(students.stream().filter(student -> equalString.equals(mapper.apply(student))).collect(Collectors.toList()));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudents(students, name, Student::getFirstName);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudents(students, name, Student::getLastName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudents(students, group, Student::getGroup);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    private Set<Map.Entry<String, List<Student>>> sortGroup(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList())).entrySet();
    }

    private List<Group> getGroups(Collection<Student> students, UnaryOperator<List<Student>> sorter) {
        return sortGroup(students).stream()
                .map(element -> new Group(element.getKey(), sorter.apply(element.getValue()))).collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroups(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroups(students, this::sortStudentsById);
    }

    private String getLargest (Collection<Student> students, ToIntFunction<List<Student>> filter){
        return sortGroup(students).stream().map(element -> Map.entry(element.getKey(), filter.applyAsInt(element.getValue())))
                .max(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).orElse("");
    }
    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargest(students, List::size);
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargest(students, list -> getDistinctFirstNames(list).size());
    }

}
