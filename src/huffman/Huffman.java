package huffman;


import fs.DataAccess;
import fs.DataFile;

import javax.xml.crypto.Data;


/**
 * propose un algorithme de compression sans perte basé sur l'algorithme de huffman
 * un  fichier compréssé comprend 4 octer qui donne la taille en bit de la partie compressé du fichier
 * on donne ensuite l'arbre de huffman les boolean dans l'arbre sont codé en byte ce qui n'est pas o
 * puis le fichier
 */
public class Huffman {

    private DataAccess dataAccess;
    private DataFile file;
    private DataFile directory;
    public Huffman(DataAccess dataAccess, DataFile file, DataFile directory){
        this.dataAccess = dataAccess;
        this.file = file;
        this.directory = directory;
    }

    public boolean zipFile(){
        if (file.getAttribut()[4]){
            return false; //c'est un repertoire
        }
        else if (file.getExtention().startsWith("huf")){
            return false; //c'est un fichier déja compréssé
        }
        else{
            String data = dataAccess.readFileContent(file);
            LinkedList<Character> listOfCharacter = stringToLinkedList(data);
            ArbreHuffman arbreHuffman = huffman(listOfCharacter);
            LinkedList<Couple<Character, LinkedList<Boolean>>> tableCodage = codageArbre(arbreHuffman);
            LinkedList<Boolean> codeHuffman = code(listOfCharacter, tableCodage);
            dataAccess.addFile(directory, file.getName(), "zip", file.getAttribut());
            byte[] dataSize = new byte[4];
            writeBytes(dataSize, 0, 4, codeHuffman.getSize());

            return true;
        }
    }

    public void unzipFile(){

    }

    //-------------------------------------------------------------------------------------------------------------
    //auxiliar function

    private LinkedList<Boolean> assoc(LinkedList<Couple<Character, LinkedList<Boolean>>> tableAssociation , char aChar){
        if (tableAssociation.isEmpty()){ throw new  IllegalArgumentException() ; }
        else {
            Couple<Character, LinkedList<Boolean>> head = tableAssociation.getHead();
            LinkedList<Couple<Character, LinkedList<Boolean>>> tail = tableAssociation.getTail();
            if (head.element1 == aChar){
                return head.element2;
            }
            else{
                return assoc(tail, aChar);
            }
        }
    }

    private LinkedList<Boolean> code(LinkedList<Character> data, LinkedList<Couple<Character, LinkedList<Boolean>>> tableCodage){

        if (data.isEmpty()) {
            return new LinkedList<>();
       }
        else {
            char head = data.head;
            LinkedList<Character> tail = data.tail;
            return assoc(tableCodage, head).concatenne(code(tail, tableCodage));
       }

    }


    //--------------------------------------------------------------------------------------------------------------

    private Couple<Character, LinkedList<Boolean>> auxilarDecode(LinkedList<Boolean> codeMessage, ArbreHuffman arbreHuffman){
        if (arbreHuffman.isLeaf()){
            return new Couple<>(arbreHuffman.getLeaf().getaChar(), codeMessage);
        }
        else {
            ArbreHuffman arbreGauche = arbreHuffman.getNode().arbreGauche;
            ArbreHuffman arbreDroite = arbreHuffman.getNode().arbreDroite;
            boolean head = codeMessage.getHead();
            LinkedList<Boolean> tail = codeMessage.getTail();
            if (head){
                return auxilarDecode(tail, arbreDroite);
            }
            else{
                return auxilarDecode(tail, arbreGauche);
            }
        }


    }

    private LinkedList<Character> decode(LinkedList<Boolean> codeMessage, ArbreHuffman arbreHuffman){
        if (codeMessage.isEmpty()) return new LinkedList<>();
        else {
            boolean head = codeMessage.getHead();
            LinkedList<Boolean> tail = codeMessage.getTail();
            Couple<Character, LinkedList<Boolean>> charAndTail = auxilarDecode(codeMessage, arbreHuffman);
            return decode(charAndTail.element2, arbreHuffman).addElement(charAndTail.element1);
        }
    }


    //--------------------------------------------------------------------------------------------------------------

    private LinkedList<Couple<Character, LinkedList<Boolean>>> auxiliarCodageArbre(boolean bit,  LinkedList<Couple<Character, LinkedList<Boolean>>> list){
        if (list.isEmpty()){
            return new LinkedList<>();
        }
        else {
            Couple<Character, LinkedList<Boolean>> head = list.getHead();
            LinkedList<Couple<Character, LinkedList<Boolean>>> tail = list.getTail();
            return auxiliarCodageArbre(bit, tail).addElement(new Couple<>(head.getElement1(), head.getElement2().addElement(bit)));
        }
    }

    private LinkedList<Couple<Character, LinkedList<Boolean>>> codageArbre(ArbreHuffman arbreHuffman){
        if (arbreHuffman.isLeaf()){
            return  (new LinkedList<Couple<Character, LinkedList<Boolean>>>()).addElement(new Couple<>(arbreHuffman.getLeaf().getaChar(), new LinkedList<Boolean>()));
        }
        else {
            ArbreHuffman arbreGauche = arbreHuffman.getNode().arbreGauche;
            ArbreHuffman arbreDroite = arbreHuffman.getNode().arbreDroite;
            LinkedList<Couple<Character, LinkedList<Boolean>>> codageGauche = codageArbre(arbreGauche);
            LinkedList<Couple<Character, LinkedList<Boolean>>> codageDroite = codageArbre(arbreDroite);
            return auxiliarCodageArbre(true, codageDroite).concatenne(auxiliarCodageArbre(false, codageGauche));
        }
    }
    //--------------------------------------------------------------------------------------------------------------

    private int poidArbre(ArbreHuffman arbreHuffman){
        if (arbreHuffman.isLeaf()){
            return arbreHuffman.getLeaf().getNbOccur();
        }
        else {
            ArbreHuffman arbreGauche = arbreHuffman.getNode().getArbreGauche();
            ArbreHuffman arbreDroite = arbreHuffman.getNode().getArbreDroite();
            return poidArbre(arbreGauche) + poidArbre(arbreDroite);
        }
    }
    //--------------------------------------------------------------------------------------------------------------



    private LinkedList<ArbreHuffman> insererArbre(ArbreHuffman arbreHuffman, LinkedList<ArbreHuffman> listArbre){
        int poid = poidArbre(arbreHuffman);
        if (listArbre.isEmpty()){
            return new LinkedList<ArbreHuffman>().addElement(arbreHuffman);
        }
        else{
            ArbreHuffman head = listArbre.getHead();
            LinkedList<ArbreHuffman> tail = listArbre.getTail();
            if (poidArbre(head) < poid) return insererArbre(arbreHuffman, tail).addElement(head);
            else return listArbre.addElement(arbreHuffman);
        }
    }


    private LinkedList<ArbreHuffman> trier(LinkedList<ArbreHuffman> listArbre){
        if (listArbre.isEmpty()) return new LinkedList<>();
        else return insererArbre(listArbre.getHead(), trier(listArbre.getTail()));
    }
    //--------------------------------------------------------------------------------------------------------------

    private int auxiliarTableOccurence(LinkedList<Character> chaine, char actualCharacter){
        if (chaine.isEmpty()) return 0;
        else {
            char head = chaine.getHead();
            LinkedList<Character> tail = chaine.getTail();
            if (head == actualCharacter) return 1 + auxiliarTableOccurence(tail, actualCharacter);
            else return auxiliarTableOccurence(tail, actualCharacter);
        }
    }

    private LinkedList<ArbreHuffman> supprimerDoublon(LinkedList<ArbreHuffman> actualList, char actualCharacter){
        if (actualList.isEmpty()) return new LinkedList<>();
        else if (actualList.getHead().isLeaf()){
            ArbreHuffman head = actualList.getHead();
            LinkedList<ArbreHuffman> tail = actualList.getTail();
            if (head.getLeaf().getaChar() == actualCharacter) return supprimerDoublon(tail, actualCharacter);
            else return supprimerDoublon(tail, actualCharacter).addElement(head);
        }
        else throw new IllegalArgumentException();
    }

    private LinkedList<ArbreHuffman> tableOccurence(LinkedList<Character> chaine){
        if (chaine.isEmpty()){
            return new LinkedList<>();
        }
        else{
            char head = chaine.getHead();
            LinkedList<Character> tail = chaine.getTail();
            return (supprimerDoublon(tableOccurence(tail), head)).addElement(new ArbreHuffman(head, 1 + auxiliarTableOccurence(tail, head)));
        }
    }
    //--------------------------------------------------------------------------------------------------------------

    private ArbreHuffman fusioner(LinkedList<ArbreHuffman> listArbre){
        if (listArbre.isEmpty()) throw new IllegalArgumentException();
        else if (listArbre.getSize() == 1) return listArbre.getHead();
        else{
            ArbreHuffman firstElement = listArbre.getHead();
            ArbreHuffman secondElement = listArbre.getTail().getHead();
            LinkedList<ArbreHuffman> tail = listArbre.getTail().getTail();
            return fusioner(insererArbre(new ArbreHuffman(firstElement, secondElement), tail));
        }
    }
    //--------------------------------------------------------------------------------------------------------------

    private ArbreHuffman huffman(LinkedList<Character> message){
        return fusioner(trier(tableOccurence(message)));
    }
    //--------------------------------------------------------------------------------------------------------------

    private LinkedList<Character> stringToLinkedList(String string){
        LinkedList<Character> res = new LinkedList<>();
        for (int i = string.length()-1; i>=0; i--){
            res.addElement(string.charAt(i));
        }
        return res;
    }
    //--------------------------------------------------------------------------------------------------------------

    /**
     * écrit sur un sector a l'index index et sur une taille size un data entière (adresse d'un élément ou autre)
     * @param sector sector sur lequel on écrit
     * @param index index dans le sector
     * @param size size de la data longeur sur laquel on écrit
     * @param data data à écrire
     */
    private void writeBytes(byte[] sector, int index, int size, int data){
        for (int i=0; i<size; i++){
            sector[size+index-i-1] = (byte) ((data >> 8*i) & 0xFF);
        }
    }

    //--------------------------------------------------------------------------------------------------------------
    //subclass

    private class LinkedList<E>{
        private E head;
        private LinkedList<E> tail;
        private int size;

        public LinkedList(){
            this.head = null;
            this.tail = null;
            this.size = 0;
        }

        private LinkedList(E head, LinkedList<E> tail){
            this.head = head;
            this.tail = tail;
            this.size = 1 + tail.size;
        }

        public LinkedList<E> addElement(E element){
            return new LinkedList<>(element, this);
        }

        public LinkedList<E> concatenne(LinkedList<E> list){
            if (isEmpty()){
                return list;
            }
            else return (tail.concatenne(list)).addElement(head);

        }

        public E getHead() {
            return head;
        }

        public LinkedList<E> getTail() {
            return tail;
        }

        public int getSize(){
            return size;
        }

        public boolean isEmpty(){
            return size == 0;
        }
    }

    private class Couple<E, T>{
        private E element1;
        private T element2;

        public Couple(E element1, T element2){
            this.element1 = element1;
            this.element2 = element2;
        }

        public E getElement1() {
            return element1;
        }

        public T getElement2() {
            return element2;
        }
    }

    private class ArbreHuffman{
        private Node node;
        private Leaf leaf;

        public ArbreHuffman(ArbreHuffman arbreGauche, ArbreHuffman arbreDroite){
            this.node = new Node(arbreGauche, arbreDroite);
        }

        public ArbreHuffman(char aChar, int nbOccur){
            this.leaf = new Leaf(aChar, nbOccur);
        }

        public boolean isLeaf(){
            return node == null && leaf != null;
        }

        public Node getNode() {
            return node;
        }

        public Leaf getLeaf() {
            return leaf;
        }

        private class Node{
            private ArbreHuffman arbreGauche;
            private ArbreHuffman arbreDroite;

            public Node(ArbreHuffman arbreGauche, ArbreHuffman arbreDroite){
                this.arbreGauche = arbreGauche;
                this.arbreDroite = arbreDroite;
            }

            public ArbreHuffman getArbreGauche() {
                return arbreGauche;
            }

            public ArbreHuffman getArbreDroite() {
                return arbreDroite;
            }
        }

        private class Leaf{
            private char aChar;
            private int nbOccur;

            public Leaf(char aChar, int nbOccur){
                this.aChar = aChar;
                this.nbOccur = nbOccur;
            }

            public char getaChar() {
                return aChar;
            }

            public int getNbOccur() {
                return nbOccur;
            }
        }

    }



}