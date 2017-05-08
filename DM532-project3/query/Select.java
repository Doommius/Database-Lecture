package query;

import heap.HeapFile;
import parser.AST_Select;
import relop.*;

/**
 * Execution plan for selecting tuples.
 */
class Select implements Plan {
  protected String[] tables;
  protected Schema[] newschema;
  protected String[] columns;
  protected Predicate[][] preads;
  public boolean isExplain;
  protected Iterator interator;
  protected Schema schema;
  /**
   * Optimizes the plan, given the parsed query.
   * 
   * @throws QueryException if validation fails
   */
  public Select(AST_Select tree) throws QueryException {

    this.isExplain = tree.isExplain;
    this.Selecter(tree);
    this.selectjoin();

  } // public Select(AST_Select tree) throws QueryException

  protected void Selecter(AST_Select tree) throws QueryException {
    this.schema = new Schema(0);
    this.preads = tree.getPredicates();
    this.tables = tree.getTables();
    this.columns = tree.getColumns();


    String[] columns = this.columns;
    this.newschema = new Schema[this.tables.length];

    for(int i = 0; i < this.tables.length; ++i) {
      this.newschema[i] = QueryCheck.tableExists(this.tables[i]);
      this.schema = Schema.join(this.schema, this.newschema[i]);
    }


    for(int j = 0; j < columns.length; ++j) {
      String columnName = columns[j];
      QueryCheck.columnExists(this.schema, columnName);
    }


    f.a(this.schema, this.preads);
  }

  protected void selectjoin() {
    this.interator = new FileScan(this.newschema[0], new HeapFile(this.tables[0]));

    int var1;
    for(var1 = 1; var1 < this.tables.length; ++var1) {
      this.interator = new SimpleJoin(this.interator, new FileScan(this.newschema[var1], new HeapFile(this.tables[var1])), new Predicate[0]);
    }

    for(var1 = 0; var1 < this.preads.length; ++var1) {
      this.interator = new Selection(this.interator, this.preads[var1]);
    }

    if(this.columns.length > 0) {
      Integer[] intlist = new Integer[this.columns.length];

      for(int j = 0; j < this.columns.length; ++j) {
        intlist[j] = Integer.valueOf(this.schema.fieldNumber(this.columns[j]));
      }

      this.interator = new Projection(this.interator, intlist);
    }

  }

  /**
   * Executes the plan and prints applicable output.
   */
  public void execute() {
    if(isExplain) {
      this.interator.explain(0);
    } else {
      int var1 = this.interator.execute();
      System.out.println("\n" + var1 + " rows affected.");
    }
  } // public void execute()

} // class Select implements Plan
