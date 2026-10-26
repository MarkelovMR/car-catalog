package cars;

import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** Двусвязный список: каждый узел хранит ссылки на обоих соседей. */
public class ChainList<E> extends AbstractList<E> {
    private Node<E> first;
    private Node<E> last;
    private int count;

    private static final class Node<E> {
        private E value;
        private Node<E> previous;
        private Node<E> next;

        private Node(E value, Node<E> previous, Node<E> next) {
            this.value = value;
            this.previous = previous;
            this.next = next;
        }
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public E get(int index) {
        return nodeAt(index).value;
    }

    @Override
    public E set(int index, E element) {
        Node<E> node = nodeAt(index);
        E old = node.value;
        node.value = element;
        return old;
    }

    @Override
    public void add(int index, E element) {
        if (index < 0 || index > count) {
            throw new IndexOutOfBoundsException("Индекс: " + index + ", размер: " + count);
        }
        Node<E> right = index == count ? null : nodeAt(index);
        Node<E> left = right == null ? last : right.previous;
        Node<E> added = new Node<>(element, left, right);
        if (left == null) {
            first = added;
        } else {
            left.next = added;
        }
        if (right == null) {
            last = added;
        } else {
            right.previous = added;
        }
        count++;
        modCount++;
    }

    @Override
    public E remove(int index) {
        return unlink(nodeAt(index));
    }

    private E unlink(Node<E> node) {
        if (node.previous == null) {
            first = node.next;
        } else {
            node.previous.next = node.next;
        }
        if (node.next == null) {
            last = node.previous;
        } else {
            node.next.previous = node.previous;
        }
        E removed = node.value;
        node.previous = null;
        node.next = null;
        node.value = null;
        count--;
        modCount++;
        return removed;
    }

    private Node<E> nodeAt(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Индекс: " + index + ", размер: " + count);
        }
        // Начинаем обход с ближайшего конца списка.
        if (index < count / 2) {
            Node<E> node = first;
            for (int i = 0; i < index; i++) {
                node = node.next;
            }
            return node;
        }
        Node<E> node = last;
        for (int i = count - 1; i > index; i--) {
            node = node.previous;
        }
        return node;
    }

    @Override
    public void clear() {
        if (count > 0) {
            first = null;
            last = null;
            count = 0;
            modCount++;
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private Node<E> nextNode = first;
            private Node<E> returned;
            private int expectedChanges = modCount;

            private void checkChanges() {
                if (expectedChanges != modCount) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public boolean hasNext() {
                checkChanges();
                return nextNode != null;
            }

            @Override
            public E next() {
                checkChanges();
                if (nextNode == null) {
                    throw new NoSuchElementException();
                }
                returned = nextNode;
                nextNode = nextNode.next;
                return returned.value;
            }

            @Override
            public void remove() {
                checkChanges();
                if (returned == null) {
                    throw new IllegalStateException();
                }
                unlink(returned);
                returned = null;
                expectedChanges = modCount;
            }
        };
    }
}
